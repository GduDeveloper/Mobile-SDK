package com.gdu.demo.views;

public interface OnJoystickListener  {

    /** Called when the joystick is touched.
     * @param joystick The joystick which has been touched.
     * @param pX The x coordinate of the knob. Values are between -1 (left) and 1 (right).
     * @param pY The y coordinate of the knob. Values are between -1 (down) and 1 (up).
     */
    public void onTouch(final JoystickView joystick, final float pX, final float pY);
}
