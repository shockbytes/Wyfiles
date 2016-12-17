package mc.fhooe.at.wyfiles.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import mc.fhooe.at.wyfiles.R;
import mc.fhooe.at.wyfiles.util.Game;

/**
 * @author Martin Macheiner
 *         Date: 06.01.2016.
 */
public class GamesAdapter extends RecyclerView.Adapter<GamesAdapter.ViewHolder> {

    public interface OnItemClickListener {

        void onItemClick(Game g, View v);
    }

    public interface OnItemLongClickListener {

        void onItemLongClick(Game g, View v);
    }

    private ArrayList<Game> data;
    private Context context;
    private final LayoutInflater inflater;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    //----------------------------------------------------------------------

    public GamesAdapter(Context context, List<Game> data) {

        inflater = LayoutInflater.from(context);
        this.context = context;

        this.data = new ArrayList<>();
        setData(data);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.recycleritem_game, parent, false));
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
    public void addEntity(int i, Game entity) {
        data.add(i, entity);
        notifyItemInserted(i);
    }

    public void addEntityAtLast(Game entity) {
        addEntity(data.size(), entity);
    }

    public void addEntityAtFirst(Game entity) {
        addEntity(0, entity);
    }

    public void deleteEntity(Game book) {
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
        Game temp = data.remove(i);
        data.add(dest, temp);
        notifyItemMoved(i, dest);
    }

    public void setData(List<Game> data) {

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
            Game entity = data.get(i);
            int location = getLocation(this.data, entity);
            if (location < 0) {
                addEntity(i, entity);
            } else if (location != i) {
                moveEntity(i, location);
            }
        }
    }

    private int getLocation(List<Game> data, Game searching) {

        for (int j = 0; j < data.size(); ++j) {
            Game newEntity = data.get(j);
            if (searching.equals(newEntity)) {
                return j;
            }
        }

        return -1;
    }

    //----------------------------------------------------------------------
    public class ViewHolder extends RecyclerView.ViewHolder {

        private Game game;

        @Bind(R.id.reclyceritem_game_img_icon)
        protected ImageView imgView;

        @Bind(R.id.reclyceritem_game_txt_title)
        protected TextView txtTitle;

        @Bind(R.id.reclyceritem_game_txt_description)
        protected TextView txtDescription;

        public ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(game, itemView);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null) {
                        onItemLongClickListener.onItemLongClick(game, itemView);
                    }
                    return true;
                }
            });
        }

        public void bind(Game g) {

            game = g;

            txtTitle.setText(g.getName());
            txtDescription.setText(g.getDescription());
            imgView.setImageResource(g.getIconId());
        }

    }

}