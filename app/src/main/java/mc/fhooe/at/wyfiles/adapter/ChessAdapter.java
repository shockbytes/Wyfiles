package mc.fhooe.at.wyfiles.adapter;

import android.content.Context;
import android.graphics.Color;
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
import mc.fhooe.at.wyfiles.games.chess.ChessField;
import mc.fhooe.at.wyfiles.games.chess.ChessFigure;

/**
 * @author Martin Macheiner
 *         Date: 06.01.2016.
 */
public class ChessAdapter extends RecyclerView.Adapter<ChessAdapter.ViewHolder> {

    public interface OnItemClickListener {

        void onItemClick(ChessField f, View v, int pos);
    }

    public interface OnItemLongClickListener {

        void onItemLongClick(ChessField f, View v);
    }

    private ArrayList<ChessField> data;
    private Context context;
    private final LayoutInflater inflater;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    //----------------------------------------------------------------------

    public ChessAdapter(Context context, List<ChessField> data) {

        inflater = LayoutInflater.from(context);
        this.context = context;

        this.data = new ArrayList<>();
        setData(data);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.recycleritem_chessfield, parent, false));
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
    public void addEntity(int i, ChessField entity) {
        data.add(i, entity);
        notifyItemInserted(i);
    }

    public void addEntityAtLast(ChessField entity) {
        addEntity(data.size(), entity);
    }

    public void addEntityAtFirst(ChessField entity) {
        addEntity(0, entity);
    }

    public ChessField getItemAtPosition(int field) {

        if (field < 0 || field > data.size()) {
            throw new IndexOutOfBoundsException("Field " + field +" cannot be > " + data.size());
        }
        return data.get(field);
    }

    public void deleteEntity(ChessField book) {
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
        ChessField temp = data.remove(i);
        data.add(dest, temp);
        notifyItemMoved(i, dest);
    }

    public void setData(List<ChessField> data) {

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
            ChessField entity = data.get(i);
            int location = getLocation(this.data, entity);
            if (location < 0) {
                addEntity(i, entity);
            } else if (location != i) {
                moveEntity(i, location);
            }
        }
    }

    private int getLocation(List<ChessField> data, ChessField searching) {

        for (int j = 0; j < data.size(); ++j) {
            ChessField newEntity = data.get(j);
            if (searching.equals(newEntity)) {
                return j;
            }
        }

        return -1;
    }

    //----------------------------------------------------------------------
    public class ViewHolder extends RecyclerView.ViewHolder {

        private ChessField field;

        private int position;

        @Bind(R.id.reclyceritem_chessfield_img)
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

        public void bind(ChessField field, int position) {

            this.field = field;
            this.position = position;

            int bg = calculateBackground();
            imgView.setBackgroundResource(bg);

            if (field.getFigure() != null) {
                imgView.setImageResource(field.getFigure().getIcon());
                if (field.getFigure().getColor() == ChessFigure.Color.BLACK) {
                    imgView.setColorFilter(Color.parseColor("#424242"));
                }
            } else {
                imgView.setImageResource(0);
                imgView.clearColorFilter();
            }
        }

        private int calculateBackground() {

            int row = (int) Math.ceil(position/8);
            int col = position%8;

            if (row%2 == 0) {
                if (field.isHighlighted()) {
                    return (col%2 == 0)
                            ? R.drawable.chess_field_bg_light_selected
                            : R.drawable.chess_field_bg_dark_selected;
                } else {
                    return (col%2 == 0)
                            ? R.color.chess_field_light
                            : R.color.chess_field_dark;
                }
            } else {
                if (field.isHighlighted()) {
                    return (col%2 == 0)
                            ? R.drawable.chess_field_bg_dark_selected
                            : R.drawable.chess_field_bg_light_selected;
                } else {
                    return (col%2 == 0)
                            ? R.color.chess_field_dark
                            : R.color.chess_field_light;
                }
            }
        }

    }

}