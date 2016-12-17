package mc.fhooe.at.wyfiles.fragments;


import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import mc.fhooe.at.wyfiles.R;
import mc.fhooe.at.wyfiles.adapter.GamesAdapter;
import mc.fhooe.at.wyfiles.util.Game;
import mc.fhooe.at.wyfiles.util.ResourceManager;

public class GamesFragment extends Fragment implements GamesAdapter.OnItemClickListener {

    public static GamesFragment newInstance() {
        GamesFragment fragment = new GamesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Bind(R.id.fragment_games_recyclerview)
    protected RecyclerView recyclerView;

    private GamesAdapter gamesAdapter;

    public GamesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_games, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews();
    }

    @Override
    public void onDestroyView() {
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    private void initializeViews() {

        // Initialize RecyclerView
        List<Game> games = ResourceManager.loadGames(getContext());
        gamesAdapter = new GamesAdapter(getContext(), games);
        recyclerView.setLayoutManager(getLayoutManager());
        gamesAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(gamesAdapter);
    }

    private RecyclerView.LayoutManager getLayoutManager() {

        return (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                ? new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)
                : new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
    }

    @Override
    public void onItemClick(Game g, View v) {

        Toast.makeText(getContext(), "Start " + g.getName(), Toast.LENGTH_SHORT).show();
    }
}
