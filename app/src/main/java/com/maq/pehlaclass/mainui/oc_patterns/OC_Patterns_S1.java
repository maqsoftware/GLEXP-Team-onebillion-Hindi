package com.maq.pehlaclass.mainui.oc_patterns;

import com.maq.pehlaclass.controls.OBControl;
import com.maq.pehlaclass.mainui.generic.OC_Generic;
import com.maq.pehlaclass.mainui.generic.OC_Generic_CompleteSequence;

import java.util.EnumSet;
import java.util.Locale;

/**
 * Created by pedroloureiro on 12/07/16.
 */
public class OC_Patterns_S1 extends OC_Generic_CompleteSequence
{

    public void demo1a () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        demoButtons();
        //
        action_playNextDemoSentence(false); // Now look at this pattern.
        OC_Generic.pointer_moveToObjectByName("obj_1", -25, 1.2f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        for (int i = 1; i < 7; i++)
        {
            action_playNextDemoSentence(true); // Leaf. Flower. Leaf. Flower. Leaf. Flower.
            if (i < 6)
            {
                OC_Generic.pointer_moveToObjectByName(String.format(Locale.US,"obj_%d", i + 1), -25 + i * 5, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
            }
        }
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // So a leaf comes next;
        OC_Generic.pointer_moveToObjectByName("obj_7", -15, 0.6f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        action_playNextDemoSentence(false); // Like this
        OC_Generic.pointer_moveToObjectByName("obj_7", -15, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_MIDDLE), true, this);
        //
        OBControl place = objectDict.get("place_1");
        OBControl control = objectDict.get("obj_7");
        OBControl dash = objectDict.get("dash");
        //
        OC_Generic.pointer_moveToPointWithObject(control, place.position(), 0, 0.6f, true, this);
        playSfxAudio("correct", false);
        dash.hide();
        OC_Generic.pointer_moveToObjectByName("obj_7", 0, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        nextScene();
    }


    public void demo1f () throws Exception
    {
        setStatus(STATUS_DOING_DEMO);
        loadPointer(POINTER_MIDDLE);
        //
        action_playNextDemoSentence(false); // Now look at this pattern;
        OC_Generic.pointer_moveToObjectByName("obj_1", -25, 1.2f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
        waitAudio();
        waitForSecs(0.3);
        //
        for (int i = 1; i < 8; i++)
        {
            action_playNextDemoSentence(true); // Shoe. Shoe. Sock. Sock. Shoe. Shoe. Sock.
            if (i < 7)
            {
                OC_Generic.pointer_moveToObjectByName(String.format(Locale.US,"obj_%d", i + 1), -25 + i * 5, 0.3f, EnumSet.of(OC_Generic.Anchor.ANCHOR_BOTTOM), true, this);
            }
        }
        waitForSecs(0.3);
        //
        thePointer.hide();
        waitForSecs(0.7);
        //
        setStatus(STATUS_AWAITING_CLICK);
        //
        doAudio(currentEvent());
    }


}
