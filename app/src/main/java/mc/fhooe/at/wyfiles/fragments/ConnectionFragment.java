package mc.fhooe.at.wyfiles.fragments;


import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;
import mc.fhooe.at.wyfiles.R;
import mc.fhooe.at.wyfiles.core.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectionFragment extends Fragment {


    public ConnectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_connection, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick(R.id.fragment_connection_imgbtn_develop)
    public void onClickDeveloperAccess() {

        startActivity(MainActivity.newIntent(getContext(), null, false),
                ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity()).toBundle());
        getActivity().supportFinishAfterTransition();
    }

}
