package com.w2mem.app;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.w2mem.app.data.DatabaseHelper;
import com.w2mem.app.data.SettingsHelper;
import com.w2mem.app.system.WordPair;

import java.util.Collections;
import java.util.List;

public class TrainDict extends BaseActivity implements View.OnClickListener {

    /* USER INTERFACE */
    private EditText uiEditWord;
    private TextView uiTextWord;
    private Button uiButtonCheckAnswer;
    private Button uiShowMenu;
    private TextView uiTextStatus;

    /* CONSTANTS */
    public static final String DICT_ID = "dict_id";

    /* DATA */
    private Resources res;

    private long dictId;
    private List<WordPair> wordPairs;
    private String translateHint;
    private boolean tryAgain = false;
    private boolean isShuffled = false;
    private int correctWordPairsNum;
    private int currentWordPairCount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.v1_test_activity);

        // user interface binding
        uiEditWord = (EditText) findViewById(R.id.etAnswer);
        uiTextWord = (TextView) findViewById(R.id.tvTask);
        uiTextStatus = (TextView) findViewById(R.id.tvStatus);
        uiButtonCheckAnswer = (Button) findViewById(R.id.btnCheck);
        uiShowMenu = (Button) findViewById(R.id.btnShowMenu);

        uiButtonCheckAnswer.setOnClickListener(this);
        uiShowMenu.setOnClickListener(this);

        // loads resources and default hints strings
        res = getResources();
        translateHint = res.getString(R.string.tvTask) + ": ";

        // gets dictId from intention and loads word pairs from database
        dictId = getIntent().getExtras().getLong(DICT_ID);
        getWordPairsFromDatabase();

        isShuffled = SettingsHelper.getShuffleState();
        if (isShuffled) {
            isShuffled = false;
            shuffle();
        }

        startTask();
    }

    /* Click handler */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCheck:
                if (tryAgain) {
                    startTask();
                    uiButtonCheckAnswer.setText(res.getString(R.string.btnCheck));
                    tryAgain = false;
                } else {
                    nextTask();
                }
                break;

            case R.id.btnShowMenu:
                openOptionsMenu();
                break;
        }
    }

    protected boolean checkAnswer() {
        String word = uiEditWord.getText().toString();
        word = word.toLowerCase().trim();
        return word.equals(getCurrentWordTranslation());
    }

    private void nextTask() {
        String hint = null;
        boolean correct = checkAnswer();
        if (correct) {
            correctWordPairsNum++;
            hint = res.getString(R.string.hintTrue);
        } else {
            hint = res.getString(R.string.hintFalse) + " " +
                res.getString(R.string.hintCorrectTranslate) + ": " +
                getCurrentWordTranslation();
        }
        // resets edit field and shows hint
        uiEditWord.setText("");
        uiEditWord.setHint(hint);
        if (currentWordPairCount != wordPairs.size() - 1) {
            currentWordPairCount++;
            uiTextWord.setText(translateHint + getCurrentWord());
        } else {
            // if last one - displays "Well done!" hint and changes button to "Try again"
            uiTextWord.setText(res.getText(R.string.achiveGoodJob));
            uiButtonCheckAnswer.setText(res.getText(R.string.btnTryAgain));
            tryAgain = true;
        }
        updateBottomHint();
    }

    private void startTask() {
        // resets test values for the new task
        currentWordPairCount = 0;
        correctWordPairsNum = 0;
        uiTextWord.setText(translateHint + wordPairs.get(0).word);
        //uiEditWord.setHint(wordPairs.get(0).getWord());
        uiEditWord.setText("");
        updateBottomHint();
    }

    public void updateBottomHint() {
        String totalCountHint = res.getString(R.string.tvTotalCount) + ": ";
        String rightCountHint = res.getString(R.string.tvGoodCount) + ": ";
        uiTextStatus.setText(
            totalCountHint + (currentWordPairCount + 1) + "/" + wordPairs.size() + " " +
                rightCountHint + correctWordPairsNum);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.testmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnSwap:
                swap();
                break;
            case R.id.mnShuffle:
                shuffle();
                if (isShuffled) {
                    item.setTitle(res.getString(R.string.mnShuffleOn));
                } else {
                    item.setTitle(res.getString(R.string.mnShuffleOff));
                }
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        startTask();
        return true;
    }

    /* swaps languages in word pairs */
    private void swap() {
        for (WordPair pair : wordPairs) {
            String word = pair.word;
            String translation = pair.translation;
            pair.word = translation;
            pair.translation = word;
        }
    }

    /* shuffles word pairs */
    private void shuffle() {
        // switches shuffle state
        isShuffled = !isShuffled;
        // saves shuffle option value to app preferences
        SettingsHelper.saveShuffleState(isShuffled);
        if (isShuffled) {
            Collections.shuffle(wordPairs);
        } else {
            // if shuffle has been turned off - restores original word pairs order
            getWordPairsFromDatabase();
        }
    }

    private void getWordPairsFromDatabase() {
        wordPairs = DatabaseHelper.getAllWordPairsFromDict(dictId);
    }

    private String getCurrentWord() {
        return wordPairs.get(currentWordPairCount).word;
    }

    private String getCurrentWordTranslation() {
        return wordPairs.get(currentWordPairCount).translation.toLowerCase();
    }
}
