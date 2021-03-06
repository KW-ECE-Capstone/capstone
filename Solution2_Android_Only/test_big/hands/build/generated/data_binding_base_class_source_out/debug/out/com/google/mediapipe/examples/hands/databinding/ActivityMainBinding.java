// Generated by view binder compiler. Do not edit!
package com.google.mediapipe.examples.hands.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.google.mediapipe.examples.hands.R;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityMainBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final Button btnYStart;

  @NonNull
  public final FrameLayout previewDisplayLayout;

  @NonNull
  public final YouTubePlayerView youtubePlayerView;

  private ActivityMainBinding(@NonNull LinearLayout rootView, @NonNull Button btnYStart,
      @NonNull FrameLayout previewDisplayLayout, @NonNull YouTubePlayerView youtubePlayerView) {
    this.rootView = rootView;
    this.btnYStart = btnYStart;
    this.previewDisplayLayout = previewDisplayLayout;
    this.youtubePlayerView = youtubePlayerView;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityMainBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityMainBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_main, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityMainBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.btn_yStart;
      Button btnYStart = ViewBindings.findChildViewById(rootView, id);
      if (btnYStart == null) {
        break missingId;
      }

      id = R.id.preview_display_layout;
      FrameLayout previewDisplayLayout = ViewBindings.findChildViewById(rootView, id);
      if (previewDisplayLayout == null) {
        break missingId;
      }

      id = R.id.youtube_player_view;
      YouTubePlayerView youtubePlayerView = ViewBindings.findChildViewById(rootView, id);
      if (youtubePlayerView == null) {
        break missingId;
      }

      return new ActivityMainBinding((LinearLayout) rootView, btnYStart, previewDisplayLayout,
          youtubePlayerView);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
