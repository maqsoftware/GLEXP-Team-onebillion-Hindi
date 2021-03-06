package com.maq.pehlaclass.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import com.maq.pehlaclass.mainui.MainActivity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * OBAudioManager
 * Controls the functionality of playing audio files and waiting for those files to finish.
 * Can also preload audio into channels and retrieve the duration of the audio file
 */
public class OBAudioManager {
    public static final String AM_MAIN_CHANNEL = "0",
            AM_BACKGROUND_CHANNEL = "1",
            AM_SFX_CHANNEL = "2";
    private final static int MAX_AUDIOPATH_CACHE_COUNT = 40;
    public static OBAudioManager audioManager;
    private final Map<String, OBGeneralAudioPlayer> players;
    final private OBTextToSpeech textToSpeech;
    private Map<String, String> pathCacheDict = new HashMap<>();
    private Map<String, Boolean> pathCacheAudio = new HashMap<>();
    private List<String> pathCacheList = new ArrayList<>();
    private boolean isAudioFile;

    public OBAudioManager(Context context) {
        players = new HashMap<String, OBGeneralAudioPlayer>();
        audioManager = this;
        textToSpeech = new OBTextToSpeech(context);
    }

    public static Map<String, Object> loadAudioXML(InputStream xmlStream) throws Exception {
        Map<String, Object> audioDict = new HashMap<String, Object>();
        if (xmlStream != null) {
            OBXMLManager xmlManager = new OBXMLManager();
            Object xmlobj = xmlManager.parseFile(xmlStream);
            if (xmlobj != null) {
                Map<String, Float> sfxvols = new HashMap<>();
                List<String> evts = new ArrayList<>();
                audioDict.put("__sfxvols", sfxvols);

                List<OBXMLNode> children;
                if (xmlobj instanceof OBXMLNode)
                    children = ((OBXMLNode) xmlobj).childrenOfType("event");
                else {
                    children = ((List<OBXMLNode>) xmlobj).get(0).childrenOfType(("event"));
                }
                for (OBXMLNode xmlevent : children) {
                    String ekey = xmlevent.attributeStringValue("id");
                    evts.add(ekey);
                    Map<String, List<Object>> phrasegroups = new HashMap<String, List<Object>>();
                    List<String> groupList = new ArrayList<>();
                    for (OBXMLNode xmlphrasegroup : xmlevent.childrenOfType("phrasegroup")) {
                        String pgkey = xmlphrasegroup.attributeStringValue("id");
                        groupList.add(pgkey);
                        List<Object> phrases = new ArrayList<Object>();
                        for (OBXMLNode xmlphrase : xmlphrasegroup.childrenOfType("phrase")) {
                            String phrase = xmlphrase.contents.trim();
                            try {
                                int n = Integer.parseInt(phrase);
                                phrases.add(n);
                            } catch (Exception e) {
                                phrases.add(phrase);
                            }
                        }
                        phrasegroups.put(pgkey, phrases);
                        String volk = xmlphrasegroup.attributeStringValue("vol");
                        if (volk != null && phrases.size() > 0)
                            sfxvols.put((String) phrases.get(phrases.size() - 1), Float.parseFloat(volk));
                    }
                    phrasegroups.put("__keys", (List<Object>) (Object) groupList);
                    audioDict.put(ekey, phrasegroups);
                }
                audioDict.put("__events", evts);
            }
        }
        return audioDict;
    }

    private void stopPlayingOnChannel(String ch) {
        OBGeneralAudioPlayer player = players.get(ch);
        if (ch.equals(AM_SFX_CHANNEL) || isAudioFile)
            player.stopPlaying(true);
        else
            textToSpeech.stopAudio();
    }

    public void stopPlaying() {
        stopPlayingOnChannel(AM_MAIN_CHANNEL);
    }

    public void stopPlayingSFX() {
        stopPlayingOnChannel(AM_SFX_CHANNEL);
    }

    public void stopPlayingBackground() {
        stopPlayingOnChannel(AM_BACKGROUND_CHANNEL);
    }

    public void stopAllAudio() {
        for (String k : players.keySet())
            stopPlayingOnChannel(k);
    }

    public String getAudioPath(String fileName) {
        for (String suffix : OBConfigManager.sharedManager.getAudioExtensions()) {
            for (String path : OBConfigManager.sharedManager.getAudioSearchPaths()) {
                String fullPath = path + "/" + fileName + "." + suffix;
                AssetFileDescriptor fd = OBUtils.getAssetFileDescriptorForPath(fullPath);
                if (fd != null) {
                    return fullPath;
                }
            }
        }
        return null;
    }

    public AssetFileDescriptor getAudioPathFD(String fileName) {
        String filePath = pathCacheDict.get(fileName);
        String fullPath = "";
        AssetFileDescriptor fd = null;
        if (filePath != null)
            fd = OBUtils.getAssetFileDescriptorForPath(filePath);
        if (fd == null) {
            String audioSuffix = OBConfigManager.sharedManager.getAudioExtensions().get(0);
            String txtSuffix = OBConfigManager.sharedManager.getTextExtensions().get(0);
            for (String path : OBConfigManager.sharedManager.getAudioSearchPaths()) {
                // this checks whether the audio to be played is on sfx or on main channel
                if (path.split("/", 0)[1].equals("sfx")) {
                    fullPath = path + "/" + fileName + "." + audioSuffix;
                    fd = OBUtils.getAssetFileDescriptorForPath(fullPath);
                    isAudioFile = true;
                } else {
                    // The conditions check whether the file is a text file or an audio file
                    // (for ETPA audios)
                    fullPath = path + "/" + fileName + "." + txtSuffix;
                    fd = OBUtils.getAssetFileDescriptorForPath(fullPath);
                    if (fd != null) {
                        isAudioFile = false;
                        break;
                    }
                    fullPath = path + "/" + fileName + "." + audioSuffix;
                    fd = OBUtils.getAssetFileDescriptorForPath(fullPath);
                    isAudioFile = true;
                }
                if (fd != null)
                    break;
            }
            if (fd == null) {
                return null;
            }
            pathCacheAudio.put(fileName, isAudioFile);
            pathCacheDict.put(fileName, fullPath);
        }
        pathCacheList.remove(fileName);
        pathCacheList.add(fileName);
        if (pathCacheList.size() >= MAX_AUDIOPATH_CACHE_COUNT) {
            String firstobj = pathCacheList.get(0);
            pathCacheList.remove(0);
            pathCacheDict.remove(firstobj);
            pathCacheAudio.remove(firstobj);
        }
        return fd;
    }

    public void playOnChannel(String ch) {
        if (ch.equals(AM_SFX_CHANNEL) || isAudioFile)
            playerForChannel(ch).play();
    }

    public void startPlaying(String fileName, String channel) {
        OBGeneralAudioPlayer player = playerForChannel(channel);
        if (fileName == null)
            player.stopPlaying();
        else {
            AssetFileDescriptor fd = getAudioPathFD(fileName);
            if (fd != null) {
                if (channel.equals(AM_SFX_CHANNEL) || (pathCacheAudio.containsKey(fileName) && pathCacheAudio.get(fileName)))
                    player.startPlaying(fd);
                else
                    textToSpeech.playAudio(fd);
            } else {
                MainActivity.log("Error caught in OBAudioManager.startPlaying [" + fileName + "] returned a null file descriptor");
            }
        }
    }

    private void startPlaying(String fileName, String channel, double atTime) {
        startPlaying(fileName, channel, atTime, 1.0f);
    }

    private void startPlaying(String fileName, String channel, double atTime, float atVolume) {
        OBGeneralAudioPlayer player = playerForChannel(channel);
        if (fileName == null)
            player.stopPlaying();
        else {
            AssetFileDescriptor fd = getAudioPathFD(fileName);
            if (channel.equals(AM_SFX_CHANNEL) || (pathCacheAudio.containsKey(fileName) && pathCacheAudio.get(fileName)))
                player.startPlayingAtTimeVolume(fd, (int) (atTime * 1000), atVolume);
            else
                textToSpeech.playAudio(fd);
        }
    }

    public void startPlaying(String fileName, double atTime) {
        startPlaying(fileName, AM_MAIN_CHANNEL, atTime);
    }

    public void startPlaying(String fileName) {
        startPlaying(fileName, AM_MAIN_CHANNEL);
    }

    public void startPlayingSFX(String fileName, float vol) {
        startPlaying(fileName, AM_SFX_CHANNEL, 0, vol);
    }

    public void waitPrepared() {
        waitPrepared(AM_MAIN_CHANNEL);
    }

    public void waitUntilPlaying() {
        waitUntilPlaying(AM_MAIN_CHANNEL);
    }

    private void waitPrepared(String ch) {
        OBGeneralAudioPlayer player = players.get(ch);
        if (player != null)
            player.waitPrepared();
    }

    private void waitUntilPlaying(String ch) {
        OBGeneralAudioPlayer player = players.get(ch);
        if (isAudioFile && player != null)
            player.waitUntilPlaying();
    }

    public void waitAudioChannel(String ch) {
        OBGeneralAudioPlayer player = players.get(ch);
        if ((ch.equals(AM_SFX_CHANNEL) || isAudioFile) && player != null)
            player.waitAudio();
    }

    public void waitAudio() {
        waitAudioChannel(AM_MAIN_CHANNEL);
    }

    public void waitSFX() {
        waitAudioChannel(AM_SFX_CHANNEL);
    }

    public Boolean isPlayingChannel(String ch) {
        OBGeneralAudioPlayer player = players.get(ch);
        if ((ch.equals(AM_SFX_CHANNEL) || isAudioFile) && player != null)
            return player.isPlaying();
        else
            return textToSpeech.isPlaying();
    }

    public Boolean isPlaying() {
        return isPlayingChannel(AM_MAIN_CHANNEL);
    }

    private Boolean isPreparingChannel(String ch) {
        OBGeneralAudioPlayer player = players.get(ch);
        if (isAudioFile && player != null)
            return player.isPreparing();
        else
            return textToSpeech.isPreparing();
    }

    public Boolean isPreparing() {
        return isPreparingChannel(AM_MAIN_CHANNEL);
    }

    public double durationForChannel(String ch) {
        OBGeneralAudioPlayer player = players.get(ch);
        if (player != null)
            return player.duration();
        return 0.0;
    }

    public double duration() {
        return durationForChannel(AM_MAIN_CHANNEL);
    }


    public double durationSFX() {
        return durationForChannel(AM_SFX_CHANNEL);
    }

    public OBGeneralAudioPlayer playerForChannel(String channel) {
        OBGeneralAudioPlayer player = players.get(channel);
        if (player == null) {
            player = new OBAudioPlayer();
            synchronized (this) {
                players.put(channel, player);
            }
        }
        return player;
    }

    public void prepareForChannel(final String fileName, final String channel) {
        OBGeneralAudioPlayer player = playerForChannel(channel);
        AssetFileDescriptor fd = getAudioPathFD(fileName);
        player.prepare(fd);
    }

    public void prepare(final String fileName) {
        prepareForChannel(fileName, AM_MAIN_CHANNEL);
    }

    public void clearCaches() {
        pathCacheDict.clear();
        pathCacheList.clear();
        pathCacheAudio.clear();
        synchronized (players) {
            Set<String> tempPlayers = new HashSet(players.keySet());
            for (String s : tempPlayers) {
                if (!s.equals(AM_MAIN_CHANNEL)) {
                    players.remove(s);
                }
            }
        }
    }

    public void onStop() {
        textToSpeech.stopAudio();
    }

    public void onResume() {
        textToSpeech.onResume();
    }

    public void onDestroy() {
        textToSpeech.onDestroy();
    }

}
