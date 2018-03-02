package jp.techacademy.takashi.nakamura.qa_app;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText mEmailEditText;  // emailアドレス、アカウント名
    EditText mPasswordEditText;  // パスワード
    EditText mNameEditText;  // 表示名
    ProgressDialog mProgress;

    FirebaseAuth mAuth;  // FirebaseのAuthentication、アカウント作成やログインなどユーザーを処理するのに必要（１）
    OnCompleteListener<AuthResult> mCreateAccountListener;  // アカウント作成完了リスナー
    OnCompleteListener<AuthResult> mLoginListener;  // ログイン完了リスナー
    DatabaseReference mDataBaseReference;  // Firebaseデータベース内のロケーションへの参照

    // アカウントが作成されていればtrue、アカウント作成時にtrueにする
    // ログイン処理後にtrueなら表示名をFireBaseとPreferenceに保存する
    boolean mIsCreateAccount = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase内の参照の初期設定
        mDataBaseReference = FirebaseDatabase.getInstance().getReference();

        // FirebaseのAuthenticationの初期設定
        mAuth = FirebaseAuth.getInstance();

        // アカウント作成完了時に呼ばれるリスナー
        mCreateAccountListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // アカウント作成が成功した場合、ログインを行う
                    String email = mEmailEditText.getText().toString();
                    String password = mPasswordEditText.getText().toString();
                    login(email, password);
                } else {
                    // 失敗した場合、エラーを表示する
                    View view = findViewById(android.R.id.content);  // このActivity
                    Snackbar.make(view, "アカウント作成に失敗しました", Snackbar.LENGTH_LONG).show();
                    // プログレスダイアログを非表示にする
                    mProgress.dismiss();
                }
            }
        };

        // ログイン処理完了時に呼ばれるリスナー
        mLoginListener = new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {  // 外のif文
                    // ログインが成功した場合、ユーザーを取得し、
                    FirebaseUser user = mAuth.getCurrentUser();
                    // databaseRoot -> users(定数) -> user.getUid() でそのユーザーのユニークIDへの参照を取得
                    DatabaseReference userRef = mDataBaseReference.child(Const.UsersPATH).child(user.getUid());

                    if (mIsCreateAccount) {  // 内のif文
                        // アカウントが作成されている状態でのログイン完了の時、表示名をFirebaseに保存する
                        String name = mNameEditText.getText().toString();  // EditTextから表示名を取得

                        Map<String, String> data = new HashMap<String, String>();  // HashMapの宣言
                        data.put("name", name);  // HashMapに表示名を追加
                        userRef.setValue(data);  // ユーザーのユニークIDへの参照を使って表示名をFirebaseに登録

                        // 表示名をPreferenceに保存
                        saveName(name);
                    } else {  // 内のif文のelse
                        // アカウントが作成されていない状態のログイン完了の時、ユーザーのユニークIDに
                        //　SettingActivity(設定画面)で表示名が変更されてFirebaseへの登録されたときに
                        // 呼ばれるイベントリスナーをセット
                        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot Snapshot) {
                                // 表示名のFirebaseへの登録が成功した場合、
                                // その表示名をPreferenceにも保存
                                Map data = (Map) Snapshot.getValue();
                                saveName((String) data.get("name"));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // 表示名のFirebaseへの登録がエラーとなった場合、何もしない
                            }
                        });
                    }  // 内のif-elseの終わり
                    // プログレスダイアログを非表示
                    mProgress.dismiss();
                    // Activityを閉じる
                    finish();

                } else {  // 外のif文のelse
                    // ログインが失敗した場合、エラーを表示する
                    View view = findViewById(android.R.id.content);  // このActivity
                    Snackbar.make(view, "ログインに失敗しました", Snackbar.LENGTH_LONG).show();
                    // プログレスダイアログを非表示にする
                    mProgress.dismiss();
                }  // 外のif-elseの終わり
            }  // end of onComplete(Task<AuthResult> task)
        };  // end of OnCompleteListener<AuthResult>()

        setTitle("ログイン");

        // UIの設定
        mEmailEditText = (EditText) findViewById(R.id.emailText);
        mPasswordEditText = (EditText) findViewById(R.id.passwordText);
        mNameEditText = (EditText) findViewById(R.id.nameText);

        mProgress = new ProgressDialog(this);
        mProgress.setMessage("処理中...");

        // アカウント作成ボタンが押されたとき呼ばれるリスナー
        Button createButton = (Button) findViewById(R.id.createButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // キーボードが出てたら閉じる
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                // EditTextの文字列をそれぞれ取得
                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();
                String name = mNameEditText.getText().toString();

                // 正しく入力されているかチェック
                if (email.length() != 0 && password.length() >= 6 && name.length() != 0) {
                    // 正しく入力されていれば、アカウントが作成されている状態にする
                    mIsCreateAccount = true;
                    // アカウントを作成
                    createAccount(email, password);
                } else {
                    // 正しく入力されていなければ、正しく入力するよう表示する
                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show();
                }
            }
        });

        // ログインボタンが押されたときに呼ばれるリスナー
        Button loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // キーボードが出てたら閉じる
                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

                // EditTextの文字列をそれぞれ取得
                String email = mEmailEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                // 正しく入力されているかチェック
                if (email.length() != 0 && password.length() >= 6) {
                    // 正しく入力されていれば、フラグを落としておく
                    mIsCreateAccount = false;

                    login(email, password);
                } else {
                    // エラーを表示する
                    Snackbar.make(v, "正しく入力してください", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }  // end of onCreate()

    // Firebaseにアカウントを作成するメソッド
     private void createAccount(String email, String password) {
        // プログレスダイアログを表示する
        mProgress.show();
        // アカウントを作成し、完了時に呼ばれるリスナー(OnCompleteListener)をセット（１）mAuthが必要
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(mCreateAccountListener);
    }

    // Firebaseにログインするメソッド
    private void login(String email, String password) {
        // プログレスダイアログを表示する
        mProgress.show();
        // ログインし、完了時に呼ばれるリスナー(OnCompleteListener)をセット（１）mAuthが必要
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(mLoginListener);
    }

    // Preferanceに表示名を保存するメソッド
    private void saveName(String name) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(Const.NameKEY, name);
        editor.commit();
    }
}
