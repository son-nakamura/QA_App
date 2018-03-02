package jp.techacademy.takashi.nakamura.qa_app;

import java.io.Serializable;

public class Answer implements Serializable {
    private String mBody;  // 回答文章
    private String mName;  // 回答者名
    private String mUid;  // 回答者のユーザーID
    private String mAnswerUid;  // 回答自体のユニークID(回答自体に割り振られるID)

    public Answer(String body, String name, String uid, String answerUid) {
        mBody = body;
        mName = name;
        mUid = uid;
        mAnswerUid = answerUid;
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

    public String getAnswerUid() {
        return mAnswerUid;
    }
}
