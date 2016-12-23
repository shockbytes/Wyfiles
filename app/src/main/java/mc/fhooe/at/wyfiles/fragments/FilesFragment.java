package mc.fhooe.at.wyfiles.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import mc.fhooe.at.wyfiles.R;
import mc.fhooe.at.wyfiles.adapter.FilesAdapter;
import mc.fhooe.at.wyfiles.communication.WyfilesManager;
import mc.fhooe.at.wyfiles.core.MainActivity;
import mc.fhooe.at.wyfiles.core.WyApp;

public class FilesFragment extends Fragment implements FilesAdapter.OnItemClickListener,
        MainActivity.OnFileReceivedListener {

    public static FilesFragment newInstance() {
        FilesFragment fragment = new FilesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Bind(R.id.fragment_files_recyclerview)
    protected RecyclerView recyclerView;

    @Bind(R.id.fragment_files_txt_path)
    protected TextView txtPath;

    @Inject
    protected WyfilesManager wyfilesManager;

    private WyfilesManager.WyfilesCallback wyfilesCallback;

    private FilesAdapter filesAdapter;

    private File currentFile;

    private String receivedFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wyfiles/";

    public FilesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        wyfilesCallback = (WyfilesManager.WyfilesCallback) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WyApp) getActivity().getApplication()).getAppComponent().inject(this);
        ((MainActivity)getActivity()).registerFileListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.fragment_files, container, false);
        ButterKnife.bind(this, v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((MainActivity) getActivity()).adjustForFileExplorer(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        ((MainActivity) getActivity()).adjustForFileExplorer(false);
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

        txtPath.setSelected(true);
        filesAdapter = new FilesAdapter(getContext(), null);
        recyclerView.setLayoutManager(getLayoutManager());
        filesAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(filesAdapter);

        updateAdapterWithFolder(Environment.getExternalStorageDirectory());
    }

    private RecyclerView.LayoutManager getLayoutManager() {

        return (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
                ? new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false)
                : new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
    }

    private void updateAdapterWithFolder(File baseFile) {

        File[] files = baseFile.listFiles();
        if (files != null) {
            currentFile = baseFile;
            filesAdapter.setData(Arrays.asList(files));
            txtPath.setText(currentFile.getAbsolutePath());
        }
    }

    private void handleFileAction(File f) {
        wyfilesManager.sendFileViaWifi(f, wyfilesCallback);
        Snackbar.make(getView(), f.getName() + " is on the way!", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(File f, View v) {

        if (f.isDirectory()) {
            updateAdapterWithFolder(f);
        } else {
            handleFileAction(f);
        }
    }

    @OnClick(R.id.fragment_files_imgbtn_back)
    public void onClickFolderBack() {

        if (currentFile.getParentFile() != null
                && currentFile.getParentFile().isDirectory()) {
            updateAdapterWithFolder(currentFile.getParentFile());
        }
    }

    @Override
    public void onFileReceived(String filename) {
        updateAdapterWithFolder(currentFile);
    }

    @Override
    public void openReceiveFolder() {

        if (!currentFile.getAbsolutePath().equals(receivedFolder)) {
            updateAdapterWithFolder(new File(receivedFolder));
        }
        // The else case will be always handled from method onFileReceived()

    }
}
