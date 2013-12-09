package tk.hachikoma.setup;

/**
 * Created with IntelliJ IDEA.
 * User: hyurumi
 * Date: 11/23/13
 * Time: 4:32 PM
 * To change this template use File | Settings | File Templates.
 */

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import tk.hachikoma.R;

public class WalkthroughFragment3 extends Fragment {
    Typeface fontForIcon;
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.walkthrough_fragment3, container, false);
        fontForIcon= Typeface.createFromAsset(getActivity().getAssets(), "fonts/fontawesome-webfont.ttf");
        ((TextView)v.findViewById(R.id.icon_invited)).setTypeface(fontForIcon);
        return v;
    }

}