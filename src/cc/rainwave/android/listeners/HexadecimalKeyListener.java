package cc.rainwave.android.listeners;

import android.text.Editable;
import android.text.InputType;
import android.text.method.NumberKeyListener;
import android.view.View;

public class HexadecimalKeyListener extends NumberKeyListener {
    @Override
    public int getInputType() {
        return InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
    }

    @Override
    protected char[] getAcceptedChars() {
        return new char[] {
          '0','1','2','3','4','5','6','7','8','9',
          'A','B','C','D','E','F',
          'a','b','c','d','e','f'
        };
    }
    
    @Override
    public void clearMetaKeyState(View view, Editable content, int states) {
        
    }
}
