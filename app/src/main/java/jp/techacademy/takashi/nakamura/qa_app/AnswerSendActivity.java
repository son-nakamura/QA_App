package jp.techacademy.takashi.nakamura.qa_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

// 回答作成画面
public class AnswerSendActivity extends AppCompatActivity implements View.OnClickListener, DatabaseReference.CompletionListener {

    private EditText mAnswerEditText;  // 回答入力EditText
    private Question mQuestion;  // 質問クラスのインスタンスを格納する変数
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_answer_send);

        // QuestionDetailActivity(質問詳細画面)から渡されたIntentから、質問クラスのインスタンスを取り出す
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        setTitle("回答作成");

        mAnswerEditText = (EditText) findViewById(R.id.answerEditText);
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("投稿中...");

        // sendButtonにonClickListenerをセット、押されると（２）が呼ばれる
        Button sendButton = (Button) findViewById(R.id.sendButton);
        sendButton.setOnClickListener(this);
    }

    // （１）FirebaseのanswerにHashMapの内容をセットするのが完了したら呼ばれるメソッド
    @Override
    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
        mProgress.dismiss();

        if (databaseError == null) {
            // QuestionDetailActivity(質問詳細画面)にもどる
            finish();
        } else {
            // エラーを表示して入力待ち(このActivityにとどまる)
            Snackbar.make(findViewById(android.R.id.content), "投稿に失敗しました", Snackbar.LENGTH_LONG).show();
        }
    }

    // （２）回答投稿ボタンが押されたときに処理するメソッド
    @Override
    public void onClick(View v) {
        // キーボードが出ていたら閉じる
        InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        // （３）databaseRoot -> contents -> genre(mQuestionから取得) -> questionUid(mQuestionから取得) -> answerと
        //       answer(回答)を探索
        DatabaseReference answerRef = databaseReference.child(Const.ContentsPATH)
                .child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);

        // ユーザーのIDを格納するHashMapを宣言
        Map<String, String> data = new HashMap<String, String>();

        // 現在ログインしているユーザーのIDを、HashMapに"uid"をキーとして追加
        data.put("uid", FirebaseAuth.getInstance().getCurrentUser().getUid());

        // Preferenceからユーザーの表示名を取得し、HashMapに"name"をキーとして追加
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String name = sp.getString(Const.NameKEY, "");
        data.put("name", name);

        // 回答内容をEditTextから取得
        String answer = mAnswerEditText.getText().toString();

        if (answer.length() == 0) {
            // 回答内容が入力されていない時はエラーを表示して入力待ち(このActivityにとどまる)
            Snackbar.make(v, "回答を入力してください", Snackbar.LENGTH_LONG).show();
            return;
        }
        // 回答内容をHashMapに"body"をキーとして追加
        data.put("body", answer);

        mProgress.show();

        // （３）で探索したFirebaseのanswer(回答)にHashMapの内容をセット、完了すると（１）が呼ばれる
        answerRef.push().setValue(data, this);
    }

}
