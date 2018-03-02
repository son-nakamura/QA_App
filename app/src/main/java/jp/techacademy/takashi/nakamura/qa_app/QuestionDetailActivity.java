package jp.techacademy.takashi.nakamura.qa_app;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

// 質問詳細画面
public class QuestionDetailActivity extends AppCompatActivity {

    private ListView mListView;
    private Question mQuestion;  // 質問クラスのインスタンスを格納する変数
    private QuestionDetailListAdapter mAdapter;  // 詳細質問画面のListViewのインスタンスを格納するアダプター

    private DatabaseReference mAnswerRef;  // Firebaseからanswerを探索した結果を格納する変数

    // 回答作成画面でFirebaseのanswer(回答)に変更が行われたとき呼ばれるメソッド群
    private ChildEventListener mEventListener = new ChildEventListener() {
        // （１）回答作成画面でFirebaseにanswerが追加されたとき呼ばれる
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            // Firebaseに追加されたanswerをHashMapに入れる
            HashMap map = (HashMap) dataSnapshot.getValue();
            // （２）Firebaseに追加されたanswerのUIDを取得
            String answerUid = dataSnapshot.getKey();
            Log.d("ANSWER_UID", answerUid);
            // 今回投稿されてFirebaseに追加されたanswer(回答)と、
            // このanswerの対象となるmQuestion(質問)がArrayList<Answer>で
            // 保持している回答とで、UIDが一致するものがないかチェック
            // answerUidをログに出すと、なぜか２回同じログが出る
            for (Answer answer : mQuestion.getAnswers()) {
                // ２回目のときは何もしない
                if (answerUid.equals(answer.getAnswerUid())) {
                    return;
                }
            }

            // HashMapからそれぞれ取り出す
            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");

            // 新しいAnswerのインスタンスを生成し
            // （２）で取り出した質問クラスのインスタンスから取り出したArrayList<Answer>に追加する
            Answer answer = new Answer(body, name, uid, answerUid);
            mQuestion.getAnswers().add(answer);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_detail);

        // （２）MainActivity(ジャンル別質問ListView)から渡されたIntentから、質問クラスのインスタンスを取り出す
        Bundle extras = getIntent().getExtras();
        mQuestion = (Question) extras.get("question");

        setTitle(mQuestion.getTitle());

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        // 質問詳細画面のListVewで使うアダプターのインスタンスをコンストラクタで作成
        // 第２引数で質問クラスのインスタンスを設定
        mAdapter = new QuestionDetailListAdapter(this, mQuestion);
        mListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();

        // ＋ボタンを押したときに呼ばれる
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 現在ログインしているユーザーのIDを取得
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ユーザーがログインしていなければログイン画面に遷移
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // ユーザーがログインしていれば、質問クラスのインスタンスをIntentに加えて質問作成画面に遷移
                    Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
                    intent.putExtra("question", mQuestion);
                    startActivity(intent);
                }
            }
        });

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        // databaseRoot -> contents -> genre(mQuestionから取得) -> questionUid(mQuestionから取得) -> answerと
        // answer(回答）を探索
        mAnswerRef = databaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre()))
                .child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
        // 探索したanswerに、回答作成画面でFirebaseのanswerに変更が行われたとき呼び出されるリスナーを設定
        // 回答作成画面でFirebaseのanswerに変更が行われたとき（１）が呼ばれる
        mAnswerRef.addChildEventListener(mEventListener);
    }
}
