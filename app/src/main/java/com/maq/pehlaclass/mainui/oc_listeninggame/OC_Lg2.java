package com.maq.pehlaclass.mainui.oc_listeninggame;

import android.util.ArrayMap;

import com.maq.pehlaclass.utils.OBAudioManager;
import com.maq.pehlaclass.utils.OBPhoneme;
import com.maq.pehlaclass.utils.OBUtils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by michal on 08/01/2018.
 */

public class OC_Lg2 extends OC_Lg
{
    Map<String, OBPhoneme> tempLetterPhonemes;

    public String sectionAudioName()
    {
        return "lg2";
    }

    public void prepare()
    {
        super.prepare();
        tempLetterPhonemes = new ArrayMap<>();
        if(parameters.containsKey("trials"))
            setMaxTrials(OBUtils.getIntValue(parameters.get("trials")));

        String[] letterParams1a = null;
        if(parameters.containsKey("mode1a"))
            letterParams1a = parameters.get("mode1a").split(",");

        String[] letterParams1b = null;
        if(parameters.containsKey("mode1b"))
            letterParams1b =parameters.get("mode1b").split(",");

        if(parameters.containsKey("distractors"))
            addToDistrators(phonemesForLetters(parameters.get("distractors").split(",")));

        int size = OBUtils.getIntValue(parameters.get("size"));
        if(letterParams1a != null)
        {
            setupEventForLearning(phonemesForLetters(letterParams1a),size);
        }

        if(letterParams1b != null)
        {
            setupEventForPractice(phonemesForLetters(letterParams1b),size);
        }


        finalisePrepare();
    }

    public List<OBPhoneme> phonemesForLetters(String[] letters)
    {
        List<OBPhoneme> arr = new ArrayList<>();

        for(String letter : letters)
        {
            String audioId = String.format("alph_%s",letter.toLowerCase(Locale.US));
            String letterId = audioId;
            if(letter.toUpperCase(Locale.US).equals(letter))
                letterId = String.format(Locale.US,"cap_%s",letterId);

            if(tempLetterPhonemes.get(letterId) != null)
            {
                OBPhoneme pho = tempLetterPhonemes.get(letterId);
                arr.add(pho);
            }
            else if(OBAudioManager.audioManager.getAudioPath(audioId) != null)
            {
                OBPhoneme pho = new OBPhoneme(letter,audioId);
                tempLetterPhonemes.put(letterId,pho);
                arr.add(pho);
            }
        }
        return arr;
    }


}
