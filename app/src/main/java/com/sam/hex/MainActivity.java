package com.sam.hex;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.sam.hex.compat.Game;
import com.sam.hex.fragment.GameFragment;
import com.sam.hex.fragment.GameSelectionFragment;
import com.sam.hex.fragment.HistoryFragment;
import com.sam.hex.fragment.InstructionsFragment;
import com.sam.hex.fragment.MainFragment;
import com.sam.hex.fragment.OnlineSelectionFragment;

import static com.sam.hex.Settings.TAG;

/**
 * @author Will Harmon
 **/
public class MainActivity extends NetActivity {
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int REQUEST_CODE_REQUIRED_PERMISSIONS = 3;

    // Play variables
    private boolean mOpenAchievements = false;
    private boolean mOpenOnlineSelectionFragment = false;

    // Fragments
    private MainFragment mMainFragment;
    private GameFragment mGameFragment;
    private GameSelectionFragment mGameSelectionFragment;
    private HistoryFragment mHistoryFragment;
    private InstructionsFragment mInstructionsFragment;
    private OnlineSelectionFragment mOnlineSelectionFragment;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            mMainFragment = new MainFragment();
            mMainFragment.setInitialRotation(-120f);
            mMainFragment.setInitialSpin(50f);
            getSupportFragmentManager().beginTransaction().add(R.id.content, mMainFragment).commit();
        } else {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content);
            invalidateFragmentState(fragment);
        }

        if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_REQUIRED_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_REQUIRED_PERMISSIONS) {
            if (!hasPermissions(this, REQUIRED_PERMISSIONS)) {
                finish();
            }
        }
    }

    public void returnHome() {
        if (mMainFragment == null) mMainFragment = new MainFragment();
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    @Override
    public void onSignInSucceeded(GoogleSignInAccount googleSignInAccount) {
        super.onSignInSucceeded(googleSignInAccount);

        if (mMainFragment != null) mMainFragment.setSignedIn(true);

        if (mOpenAchievements) {
            mOpenAchievements = false;
            openAchievements();
        }

        if (mOpenOnlineSelectionFragment) {
            mOpenOnlineSelectionFragment = false;
            swapFragment(new OnlineSelectionFragment());
        }
    }

    @Override
    public void onSignInFailed(Throwable throwable) {
        super.onSignInFailed(throwable);
        if (mMainFragment != null) mMainFragment.setSignedIn(false);
    }

    public void swapFragment(Fragment newFragment) {
        invalidateFragmentState(newFragment);
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.content, newFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }

    private void invalidateFragmentState(Fragment fragment) {
        if (fragment != null) {
            if (fragment instanceof MainFragment) {
                mMainFragment = (MainFragment) fragment;
            } else if (fragment instanceof GameFragment) {
                mGameFragment = (GameFragment) fragment;
            } else if (fragment instanceof GameSelectionFragment) {
                mGameSelectionFragment = (GameSelectionFragment) fragment;
            } else if (fragment instanceof HistoryFragment) {
                mHistoryFragment = (HistoryFragment) fragment;
            } else if (fragment instanceof InstructionsFragment) {
                mInstructionsFragment = (InstructionsFragment) fragment;
            } else if (fragment instanceof OnlineSelectionFragment) {
                mOnlineSelectionFragment = (OnlineSelectionFragment) fragment;
            } else {
                Log.w(TAG, "Unknown fragment " + fragment + " attached.");
            }
        }
    }

    public void setOpenAchievements(boolean open) {
        this.mOpenAchievements = open;
    }

    public void setOpenOnlineSelectionFragment(boolean open) {
        this.mOpenOnlineSelectionFragment = open;
    }

    @Override
    public void switchToGame(@NonNull Game game) {
        Bundle b = new Bundle();
        b.putBoolean(GameFragment.NET, true);

        mGameFragment = new GameFragment();
        mGameFragment.setGame(game);
        mGameFragment.setPlayer1Type(game.getPlayer1().getType());
        mGameFragment.setPlayer2Type(game.getPlayer2().getType());
        mGameFragment.setArguments(b);

        swapFragment(mGameFragment);
    }

    /**
     * Returns true if all given permissions are available
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static class Stat {
        private long timePlayed;
        private long gamesWon;
        private long gamesPlayed;
        private int donationRank;

        public long getTimePlayed() {
            return timePlayed;
        }

        public void setTimePlayed(long timePlayed) {
            this.timePlayed = timePlayed;
        }

        public long getGamesWon() {
            return gamesWon;
        }

        public void setGamesWon(long gamesWon) {
            this.gamesWon = gamesWon;
        }

        public long getGamesPlayed() {
            return gamesPlayed;
        }

        public void setGamesPlayed(long gamesPlayed) {
            this.gamesPlayed = gamesPlayed;
        }

        public int getDonationRank() {
            return donationRank;
        }

        public void setDonationRank(int donationRank) {
            this.donationRank = donationRank;
        }
    }
}
