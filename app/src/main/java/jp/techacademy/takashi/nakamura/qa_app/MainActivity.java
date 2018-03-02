package jp.techacademy.takashi.nakamura.qa_app;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private int mGenre = 0;  // 質問のジャンルの番号

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mGenreRef;  // Firebaseのgenreへの参照
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;  // 質問を保持するクラスQuestionのArrayList
    private QuestionsListAdapter mAdapter;  // ListViewのために使用するAdapter

    //（１）Firebaseのgenreに変更があったときに呼ばれるリスナー群
    private ChildEventListener mEventListener = new ChildEventListener() {
        // genreにquestionが追加されたときに呼ばれるリスナー
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            // dataSnapshotの内容をHashMapに格納
            HashMap map = (HashMap) dataSnapshot.getValue();
            // HashMap各要素を取り出す
            String title = (String) map.get("title");
            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");
            String imageString = (String) map.get("image");
            // 画像をBase64文字列からバイナリ形式(byte[])に戻す
            byte[] bytes;
            if (imageString != null) {
                // Base64文字列がnullでなければバイナリデータに戻す
                bytes = Base64.decode(imageString, Base64.DEFAULT);
            } else {
                // Base64文字列がnullであればバイナリデータは空にしておく
                bytes = new byte[0];
            }

            // AnswerのArrayListを宣言
            ArrayList<Answer> answerArrayList = new ArrayList<Answer>();

            // 新しいHashMapを生成し、dataSnapShotを格納したHashMapの中のanswersの要素を格納する
            HashMap answerMap = (HashMap) map.get("answers");
            if (answerMap != null) {
                // 新しいHashMapがnullでないとき、そのHashMapのすべての要素に対しfor文を実行
                for (Object key: answerMap.keySet()) {
                    // answersの各要素を一時的なHashMapに格納する
                    HashMap temp = (HashMap) answerMap.get((String) key);
                    // 一時的なHashMapからbody, name, uidを取得する
                    String answerBody = (String) temp.get("body");
                    String answerName = (String) temp.get("name");
                    String answerUid = (String) temp.get("uid");
                    // 取得したbody, name, uidとkeyからAnswerのインスタンスを生成
                    Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                    // AnswerのインスタンスをAnswerのArrayListに追加
                    answerArrayList.add(answer);
                }
            }

            // title, body, name, uid, key, genre, 画像のバイナリデータ, AnswerのArrayListからQuestionのインスタンスを生成
            Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), mGenre, bytes, answerArrayList);
            // QuestionのインスタンスをQuestionを保持するArrayListに追加
            mQuestionArrayList.add(question);
            // ListViewに変更を通知
            mAdapter.notifyDataSetChanged();
        } // end of onChildAdded()

        // genreのquestionが変更されたときに呼ばれるリスナー
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            // dataSnapshotの内容をHashMapに格納
            HashMap map = (HashMap) dataSnapshot.getValue();
            // 変更があったquestionをfor文で探索
            for (Question question : mQuestionArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {
                    // dataSnapshotから得られるkeyとkeyが一致するquestionが変更されたもの
                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    // questionからanswerを削除
                    question.getAnswers().clear();
                    // 新しいHashMapを生成し、dataSnapShotを格納したHashMapの中のanswersの要素を格納する
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        // 新しいHashMapがnullでないとき、そのHashMapのすべての要素に対しfor文を実行
                        for (Object key : answerMap.keySet()) {
                            // answersの各要素を一時的なHashMapに格納する
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            // 一時的なHashMapからbody, name, uidを取得する
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            // 取得したbody, name, uidとkeyからAnswerのインスタンスを生成
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            // AnswerのインスタンスをAnswerのArrayListに追加
                            question.getAnswers().add(answer);
                        }
                    }
                    // ListViewに変更を通知
                    mAdapter.notifyDataSetChanged();
                }
            }
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
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        // FloatingActionButtonが押されたときの処理
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ジャンルを選択していない場合(mGenre == 0)はエラーを表示してこのActivityにとどまる
                if (mGenre == 0) {
                    Snackbar.make(view, "ジャンルを選択して下さい", Snackbar.LENGTH_LONG).show();
                    return;
                }

                // ログインしているユーザーを取得する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ユーザーがログインしていなければLoginActivity(ログイン画面)に遷移
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // ユーザーがログインしていれば、Intentにジャンルを設定して
                    // QuestionSendActivity(質問作成画面)に遷移
                    Intent intent = new Intent(getApplicationContext(), QuestionSendActivity.class);
                    intent.putExtra("genre", mGenre);
                    startActivity(intent);
                }
            }
        });

        // ナビゲーションドロワーの設定
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        // ナビゲーションドロワーでアイテムが選択されたときの処理
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // 選択されたアイテムのIDを取得
                int id = item.getItemId();

                // 選択されたアイテム別にToolbarに表示する文字列とジャンルの番号を設定
                if (id == R.id.nav_hobby) {
                    mToolbar.setTitle("趣味");
                    mGenre = 1;
                } else if (id == R.id.nav_life) {
                    mToolbar.setTitle("生活");
                    mGenre = 2;
                } else if (id == R.id.nav_health) {
                    mToolbar.setTitle("健康");
                    mGenre = 3;
                } else if (id == R.id.nav_computer) {
                    mToolbar.setTitle("コンピューター");
                    mGenre = 4;
                }

                // ナビゲーションドロワーを隠す
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);

                // 質問を保持するクラスのListをクリア
                mQuestionArrayList.clear();
                // クリアしたListをAdapterにセット
                mAdapter.setQuestionArrayList(mQuestionArrayList);
                // AdapterをListViewにセット
                mListView.setAdapter(mAdapter);

                // 選択されたジャンルにリスナーを登録する
                if (mGenreRef != null) {
                    // Firebaseのgenreへの参照がnullでなければ削除する
                    mGenreRef.removeEventListener(mEventListener);
                }
                // databaseRoot -> contents -> genre でFirebaseのgenreへの参照を取得
                mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
                // genreへの参照にイベントリスナーを登録
                // genreのchildに変更されると（１）のいずれかのリスナーが呼ばれる
                mGenreRef.addChildEventListener(mEventListener);

                return true;
            }
        }); // end of setNavigationItemSelectedListener()

        // Firebaseの初期設定
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(this);  // QuestionListAdapterのインスタンス
        mQuestionArrayList = new ArrayList<Question>();
        mAdapter.notifyDataSetChanged();  // ListViewを表示させる

        // ListViewにアイテムが押されたときのリスナーを登録
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // IntentにQuestionDetailActivity(質問詳細画面)を遷移先としてセット
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                // 質問を保持するクラスのListから押された質問を取り出してIntentにセット
                intent.putExtra("question", mQuestionArrayList.get(position));
                startActivity(intent);
            }
        });
    } // end of onCreate()

    // メニューが作成されるときに呼ばれるメソッド
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // メニューがクリックされたときに呼ばれるメソッド
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // SettingActivity(設定画面)に遷移
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
            return true;
        }

        // if文の条件にあてはまらないとき(このアプリではありえない)
        return super.onOptionsItemSelected(item);
    }
}
