
package cn.zmdx.kaka.locker.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;
import cn.zmdx.kaka.locker.R;
import cn.zmdx.kaka.locker.font.FontManager;

public class BaseEditText extends EditText {

    public BaseEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public BaseEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BaseEditText(Context context) {
        this(context, null);
    }

    private void init() {
        setBackgroundResource(R.drawable.individualization_edittext_divider_line);
        setTypeface(FontManager.getChineseTypeface(getContext()));
    }
}
