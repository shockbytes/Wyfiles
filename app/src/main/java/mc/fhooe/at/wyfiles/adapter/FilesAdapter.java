package mc.fhooe.at.wyfiles.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import mc.fhooe.at.wyfiles.R;
import mc.fhooe.at.wyfiles.util.ResourceManager;

/**
 * @author Martin Macheiner
 *         Date: 06.01.2016.
 */
public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {

    public interface OnItemClickListener {

        void onItemClick(File f, View v);
    }

    public interface OnItemLongClickListener {

        void onItemLongClick(File f, View v);
    }

    private ArrayList<File> data;
    private Context context;
    private final LayoutInflater inflater;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    //----------------------------------------------------------------------

    public FilesAdapter(Context context, List<File> data) {

        inflater = LayoutInflater.from(context);
        this.context = context;

        this.data = new ArrayList<>();
        setData(data);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.recycleritem_file, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        onItemLongClickListener = listener;
    }

    //-----------------------------Data Section-----------------------------
    public void addEntity(int i, File entity) {
        data.add(i, entity);
        notifyItemInserted(i);
    }

    public void addEntityAtLast(File entity) {
        addEntity(data.size(), entity);
    }

    public void addEntityAtFirst(File entity) {
        addEntity(0, entity);
    }

    public void deleteEntity(File book) {
        int location = getLocation(data, book);
        if (location > -1) {
            deleteEntity(location);
        }
    }

    public void deleteEntity(int i) {
        data.remove(i);
        notifyItemRemoved(i);
    }

    public void moveEntity(int i, int dest) {
        File temp = data.remove(i);
        data.add(dest, temp);
        notifyItemMoved(i, dest);
    }

    public void setData(List<File> data) {

        if (data == null) {
            return;
        }

        //Remove all deleted items
        for (int i = this.data.size() - 1; i >= 0; --i) {
            //Remove all deleted items
            if (getLocation(data, this.data.get(i)) < 0) {
                deleteEntity(i);
            }
        }

        //Add and move items
        for (int i = 0; i < data.size(); ++i) {
            File entity = data.get(i);
            int location = getLocation(this.data, entity);
            if (location < 0) {
                addEntity(i, entity);
            } else if (location != i) {
                moveEntity(i, location);
            }
        }
    }

    private int getLocation(List<File> data, File searching) {

        for (int j = 0; j < data.size(); ++j) {
            File newEntity = data.get(j);
            if (searching.equals(newEntity)) {
                return j;
            }
        }

        return -1;
    }

    //----------------------------------------------------------------------
    public class ViewHolder extends RecyclerView.ViewHolder {

        private File file;

        @Bind(R.id.recycleritem_file_image)
        protected ImageView imgView;

        @Bind(R.id.recycleritem_file_txt_title)
        protected TextView txtTitle;

        @Bind(R.id.recycleritem_file_txt_meta)
        protected TextView txtMeta;

        public ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(file, itemView);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null) {
                        onItemLongClickListener.onItemLongClick(file, itemView);
                    }
                    return true;
                }
            });
        }

        public void bind(File f) {

            file = f;
            txtTitle.setText(f.getName());

            String meta;
            DateTime dt = new DateTime(f.lastModified());
            String time = DateTimeFormat.forPattern("dd MMM yyyy, kk:mm").print(dt);
            if (f.isDirectory()) {
                imgView.setImageResource(R.mipmap.ic_file_folder);
                meta = context.getString(R.string.file_metadata_folder, f.list().length, time);
            } else {
                String prefix = "Bytes";
                long size = f.length();
                if (size > 1024) {
                    size /= 1024;
                    prefix = "KB";
                }
                if (size > 1024) {
                    size /= 1024;
                    prefix = "MB";
                }
                meta = context.getString(R.string.file_metadata_file, size, prefix, time);
                imgView.setImageResource(ResourceManager.getImageIconForFileExtension(getFileExtension()));
            }

            txtMeta.setText(meta);
        }

        private String getFileExtension() {
            return file.getName().substring(file.getName().lastIndexOf(".") + 1);
        }

    }

}