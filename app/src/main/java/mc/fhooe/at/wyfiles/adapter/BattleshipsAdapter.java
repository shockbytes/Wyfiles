package mc.fhooe.at.wyfiles.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import mc.fhooe.at.wyfiles.R;
import mc.fhooe.at.wyfiles.games.BattleshipField;

/**
 * @author Martin Macheiner
 *         Date: 06.01.2016.
 */
public class BattleshipsAdapter extends RecyclerView.Adapter<BattleshipsAdapter.ViewHolder> {

    public interface OnItemClickListener {

        void onItemClick(BattleshipField f, View v, int pos);
    }

    public interface OnItemLongClickListener {

        void onItemLongClick(BattleshipField f, View v);
    }

    private ArrayList<BattleshipField> data;
    private Context context;
    private final LayoutInflater inflater;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    //----------------------------------------------------------------------

    public BattleshipsAdapter(Context context, List<BattleshipField> data) {

        inflater = LayoutInflater.from(context);
        this.context = context;

        this.data = new ArrayList<>();
        setData(data);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.recycleritem_battleships, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(data.get(position), position);
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
    public void addEntity(int i, BattleshipField entity) {
        data.add(i, entity);
        notifyItemInserted(i);
    }

    public void addEntityAtLast(BattleshipField entity) {
        addEntity(data.size(), entity);
    }

    public void addEntityAtFirst(BattleshipField entity) {
        addEntity(0, entity);
    }

    public BattleshipField getItemAtPosition(int field) {

        if (field < 0 || field > data.size()) {
            throw new IndexOutOfBoundsException("Field " + field +" cannot be > " + data.size());
        }
        return data.get(field);
    }

    public boolean isFieldAShip(int field) {
        return data.get(field).getState() == BattleshipField.FieldState.SHIP;
    }

    public void deleteEntity(BattleshipField book) {
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
        BattleshipField temp = data.remove(i);
        data.add(dest, temp);
        notifyItemMoved(i, dest);
    }

    public void setData(List<BattleshipField> data) {

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
            BattleshipField entity = data.get(i);
            int location = getLocation(this.data, entity);
            if (location < 0) {
                addEntity(i, entity);
            } else if (location != i) {
                moveEntity(i, location);
            }
        }
    }

    private int getLocation(List<BattleshipField> data, BattleshipField searching) {

        for (int j = 0; j < data.size(); ++j) {
            BattleshipField newEntity = data.get(j);
            if (searching.equals(newEntity)) {
                return j;
            }
        }

        return -1;
    }

    //----------------------------------------------------------------------
    public class ViewHolder extends RecyclerView.ViewHolder {

        private BattleshipField field;

        private int position;

        @Bind(R.id.reclyceritem_battleships_img)
        protected ImageView imgView;

        public ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(field, itemView, position);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null) {
                        onItemLongClickListener.onItemLongClick(field, itemView);
                    }
                    return true;
                }
            });
        }

        public void bind(BattleshipField field, int position) {

            this.field = field;
            this.position = position;
            imgView.setImageResource(field.getIcon());

            int bgDrawable = field.isClicked()
                    ? R.drawable.battleshipfield_background_clicked
                    : R.drawable.battleshipfield_background;
            imgView.setBackgroundResource(bgDrawable);
        }

    }

}