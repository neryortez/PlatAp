package io.github.rathn.platap.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;

import java.util.List;

import io.github.rathn.platap.MainActivity;
import io.github.rathn.platap.NewTransactionActivity;
import io.github.rathn.platap.R;
import io.github.rathn.platap.dto.Transaction;
import io.github.rathn.platap.utils.CategoryUtils;

/**
 * Created by Neri Ortez on 21/11/2016.
 */


public class TransactionAdaptor extends RecyclerView.Adapter<TransactionAdaptor.ViewHolder> {

    private final Context mContext;
    private final List<Transaction> mTransactionList;



    public TransactionAdaptor(Context context, List<Transaction> transactions) {
        mContext = context;
        mTransactionList = transactions;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View itemView = inflater.inflate(R.layout.transaction_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get the data model based on position
        Transaction t = mTransactionList.get(position);
        holder.setTransaction(t);

//        TypedArray typedArray = mContext.getResources().obtainTypedArray(R.array.icons);
//        icon.setImageDrawable(typedArray.getDrawable(index));
//        typedArray.recycle();
    }

    @Override
    public int getItemCount() {
        return mTransactionList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mDrawable;
        TextView mCaterogy;
        TextView mBalance;
        private View removible;
        private Transaction transaction;
        private CardView cardView;
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransactionInfoDialog dialog1 = new TransactionInfoDialog(mContext);
                dialog1.setTransaction(transaction);
                dialog1.show();
                /*// 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                // 2. Chain together various setter methods to set the dialog characteristics
                Context context = v.getContext();
                builder.setMessage(Html.fromHtml(context.getString(R.string.category) + ": <b> " + transaction.getCategory().getName() + "</b>" +
                        "<p>" +
                        context.getString(R.string.price) + ": <b>"
                        + transaction.getPrice() + "</b><p>" +
                        context.getString(R.string.note) + ": <i> " + transaction.getNote() + "</i>"
                ));

                builder.setNegativeButton(mContext.getString(R.string.borrar), borrarClick);
                builder.setPositiveButton(mContext.getString(R.string.modificar), modificarClick);
                builder.setNeutralButton(mContext.getString(R.string.cancelar), null);
                builder.setTitle(mContext.getString(R.string.detalles_de_transaction));
                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();*/
            }
        };


        ViewHolder(View itemView) {
            super(itemView);
            mDrawable = ((ImageView) itemView.findViewById(R.id.imageView2));
            mCaterogy = ((TextView) itemView.findViewById(R.id.cateoria));
            mBalance = ((TextView) itemView.findViewById(R.id.balance));
            cardView = ((CardView) itemView.findViewById(R.id.car_view));
            removible = itemView;
            itemView.setOnClickListener(clickListener);
            cardView.setOnClickListener(clickListener);
        }

        public View getSwipableView() {
            return removible;
        }

        public void remover(final Context context, View view, final int position) {
            if (!transaction.isRepeating()) {
                removerConfirmado(position, context);
                Snackbar.make(view, R.string.delete_warning, Snackbar.LENGTH_SHORT).setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deshacerRemover(transaction, position, context);
                    }
                }).show();
            } else {
                new AlertDialog.Builder(mContext)
                        .setTitle(R.string.delete)
                        .setMessage(R.string.delete_repeating_warning)
                        .setNegativeButton(R.string.borrar_todos, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                removerConfirmado(position, context, false);
                            }
                        })
                        .setPositiveButton(R.string.delete_just_this, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                removerConfirmado(position, context);
                            }
                        }).show();
            }
        }

        private void deshacerRemover(Transaction trans, int pos, Context context) {
            MainActivity mainActivity = (MainActivity) context;
            trans = trans.copyWithCategory();
            mainActivity.mDatabaseManager.updateTransaction(trans, trans, true);
            mainActivity.transactionList.add(pos, trans);
            mainActivity.mRecyclerView.getAdapter().notifyItemInserted(pos);
            mainActivity.findViewById(R.id.message).setVisibility(View.GONE);

            Intent intent = new Intent(CompactCalendarView.TRANSACTIONS_RE_INSERTED);
            intent.putExtra("value", trans.getPrice());

            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }

        private void removerConfirmado(int i, Context context){removerConfirmado(i, context, true);}
        private void removerConfirmado(int i, Context context, boolean onlyThis) {
            MainActivity mainActivity = (MainActivity) context;
            mainActivity.transactionList.remove(i);
            mainActivity.mRecyclerView.getAdapter().notifyItemRemoved(i);
            mainActivity.mDatabaseManager.deleteTransaction(this.transaction, onlyThis);
        }

        void efectuarPastForecasted(int position){
            transaction.setForecasted(false);
            MainActivity mainActivity = (MainActivity) mContext;
//            mainActivity.transactionList.remove(position);
            mainActivity.mRecyclerView.getAdapter().notifyItemChanged(position);
            mainActivity.mDatabaseManager.updateTransaction(this.transaction, this.transaction, /*onlyThis*/true);
        }

        public Transaction getTransaction() {
            return transaction;
        }

        public void setTransaction(Transaction t) {
            this.transaction = t;
            mBalance.setText(String.valueOf(t.getPrice()));
            mCaterogy.setText(t.getCategory().getName());
            int index = t.getCategory().getIconIndex();
            mDrawable.setImageDrawable(mContext.getResources().getDrawable(CategoryUtils.getWhiteIconResourceIdForIndex(index)));
            int backgroundColor = mContext.getResources().getColor(CategoryUtils.getColorsResourceIdForIndex(t.getCategory().getColorIndex(), mContext));
            mDrawable.setBackgroundColor(backgroundColor);
            if (t.isForecasted()) cardView.setCardBackgroundColor(0xffcccccc);
            else cardView.setCardBackgroundColor(Color.WHITE);

//            removible.setBackgroundColor(backgroundColor);
        }

        private void modificarTransaction(int position, Context context) {
            Intent intent = new Intent(context, NewTransactionActivity.class);
            Bundle b = new Bundle();
            b.putParcelable(NewTransactionActivity.TRANSACTION_TO_EDIT, transaction);
            b.putParcelable(NewTransactionActivity.REPEATINFO_OF_TRANSACTION, transaction.getRepeatInfo());
            b.putInt(NewTransactionActivity.POSICION_DE_TRANSACTION, position);
            intent.putExtras(b);
            context.startActivity(intent);
        }

        private class TransactionInfoDialog extends AlertDialog.Builder{

            TransactionInfoDialog(@NonNull Context context) {
                super(context);
                this.setNegativeButton(mContext.getString(R.string.borrar), borrarClick);
                this.setPositiveButton(mContext.getString(R.string.modificar), modificarClick);
                this.setNeutralButton(mContext.getString(R.string.cancelar), null);
                this.setTitle(mContext.getString(R.string.detalles_de_transaction));
            }

            TransactionInfoDialog setTransaction(Transaction transaction){
                /*TextView text = new TextView(mContext);
                text.setText*/
                this.setMessage(Html.fromHtml(mContext.getString(R.string.category) + ": <b> " + transaction.getCategory().getName() + "</b>" +
                        "<p>" +
                        mContext.getString(R.string.price) + ": <b>"
                        + transaction.getPrice() + "</b><p>" +
                        mContext.getString(R.string.note) + ": <i> " + transaction.getNote() + "</i>"
                ));
                if (transaction.isPastForecasted()) {
                    //TODO: Agregar parte para confirmar transaccion atrasada.
                    this.setNeutralButton(R.string.confirmar, efectuarPastForecasted);
                }
                return this;
            }

            private DialogInterface.OnClickListener borrarClick = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    while (!((MainActivity) mContext).mDatabaseManager.isOpen()) {
                        continue;
                    }
                    remover(mContext, getSwipableView(), getLayoutPosition());
//                removerConfirmado(getLayoutPosition(), mContext);
                }
            };


            private DialogInterface.OnClickListener modificarClick = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    modificarTransaction(getLayoutPosition(), mContext);
                }
            };


            private DialogInterface.OnClickListener efectuarPastForecasted = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    efectuarPastForecasted(getLayoutPosition());
                }
            };

        }


    }
}
