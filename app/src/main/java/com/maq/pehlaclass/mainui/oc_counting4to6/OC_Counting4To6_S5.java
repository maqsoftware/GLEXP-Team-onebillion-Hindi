package com.maq.pehlaclass.mainui.oc_counting4to6;

import com.maq.pehlaclass.controls.OBControl;
import com.maq.pehlaclass.controls.OBLabel;
import com.maq.pehlaclass.mainui.generic.OC_Generic_SelectCorrectObject;
import com.maq.pehlaclass.utils.OB_Maths;

/**
 * Created by pedroloureiro on 21/06/16.
 */
public class OC_Counting4To6_S5 extends OC_Generic_SelectCorrectObject
{
    public OC_Counting4To6_S5 ()
    {
        super();
    }

    @Override
    public String action_getObjectPrefix()
    {
        return "number";
    }


    @Override
    public void action_prepareScene(String scene, Boolean redraw)
    {
        if (redraw)
        {
            for (OBControl number : filterControls("number.*"))
            {
                OBLabel label = action_createLabelForControl(number, 1.1f);
            }
        }
        else
        {
            try
            {
                for (OBControl control : filterControls(".*"))
                {
                    action_lowlight(control);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void action_highlight(OBControl control) throws Exception
    {
        lockScreen();
        control.highlight();
        unlockScreen();
    }


    @Override
    public void action_lowlight(OBControl control) throws Exception
    {
        lockScreen();
        control.lowlight();
        unlockScreen();
    }


    @Override
    public void action_answerIsCorrect(OBControl target) throws Exception
    {
        gotItRightBigTick(true);
        playAudioQueuedScene(currentEvent(), "CORRECT", false);
        action_animatePlatform(target, true);
        waitAudio();
        //
        playAudioQueuedScene(currentEvent(), "FINAL", true);
        //
        nextScene();
    }
}
