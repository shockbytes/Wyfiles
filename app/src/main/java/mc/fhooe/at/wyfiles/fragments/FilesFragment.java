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

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import mc.fhooe.at.wyfiles.R;
import mc.fhooe.at.wyfiles.adapter.FilesAdapter;
import mc.fhooe.at.wyfiles.util.WyFile;

public class FilesFragment extends Fragment implements FilesAdapter.OnItemClickListener {

    public static FilesFragment newInstance() {
        FilesFragment fragment = new FilesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Bind(R.id.fragment_files_recyclerview)
    protected RecyclerView recyclerView;

    private FilesAdapter filesAdapter;


    public FilesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.fragment_files, container, false);
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
        List<WyFile> games =new ArrayList<>();
        filesAdapter = new FilesAdapter(getContext(), games);
        recyclerView.setLayoutManager(getLayoutManager());
        filesAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(filesAdapter);
    }

    private RecyclerView.LayoutManager getLayoutManager() {

        return (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                ? new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)
                : new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
    }


    @Override
    public void onItemClick(WyFile f, View v) {

    }
}
