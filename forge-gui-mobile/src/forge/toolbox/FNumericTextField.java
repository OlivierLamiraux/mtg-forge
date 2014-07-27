package forge.toolbox;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

public class FNumericTextField extends FTextField {
    private int value;

    public FNumericTextField() {
        setAlignment(HAlignment.RIGHT);
    }

    public int getValue() {
        return value;
    }
    public void setValue(int value0) {
        if (value0 < 0) {
            value0 = 0;
        }
        if (value == value0) { return; }
        value = value0;
        super.setText(String.valueOf(value));
    }

    @Override
    protected boolean validate() {
        return getText().matches("\\d*");
    }

    @Override
    public void setText(String text0) {
        if (text0.length() > 0) {
            //prevent inserting non-numbers
            text0 = text0.replaceAll("\\D", "");
            if (text0.length() == 0) {
                return;
            }
        }
        super.setText(text0);
        value = Integer.parseInt(getText());
    }

    @Override
    protected void insertText(String text0) {
        if (text0.length() > 0) {
            //prevent inserting non-numbers
            text0 = text0.replaceAll("\\D", "");
            if (text0.length() == 0) {
                return;
            }
        }
        super.insertText(text0);
        value = Integer.parseInt(getText());
    }
}
