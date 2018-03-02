package jp.techacademy.takashi.nakamura.qa_app;

import java.io.Serializable;
import java.util.ArrayList;

// 質問を保持するクラス
public class Question implements Serializable {
    private String mTitle;  // 質問のタイトル
    private String mBody;  // 質問内容の文章
    private String mName;  // 質問したユーザーの名前
    private String mUid;  // 質問したユーザーのUID
    private String mQuestionUid;  // 質問のユニークID(質問自体に割り振られるID)
    private int mGenre;  // 質問が含まれるジャンル
    private byte[] mBitmapArray;  // 質問に添付された画像
    // 回答クラスのインスタンスを格納するArrayList
    // ひとつの質問に対して複数の回答が投稿される
    private ArrayList<Answer> mAnswerArrayList;

    public String getTitle() {
        return mTitle;
    }

    public String getBody() {
        return mBody;
    }

    public String getName() {
        return mName;
    }

    public String getUid() {
        return mUid;
    }

    public String getQuestionUid() {
        return mQuestionUid;
    }

    public int getGenre() {
        return mGenre;
    }

    public byte[] getImageBytes() {
        return mBitmapArray;
    }

    public ArrayList<Answer> getAnswers() {
        return mAnswerArrayList;
    }

    // コンストラクタ
    public Question(String title, String body, String name, String uid, String questionUid, int genre, byte[] bytes, ArrayList<Answer> answers) {
        mTitle = title;
        mBody = body;
        mName = name;
        mUid = uid;
        mQuestionUid = questionUid;
        mGenre = genre;
        mBitmapArray = bytes.clone();
        mAnswerArrayList = answers;
    }
}
