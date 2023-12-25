package com.example.thaiocrscanner;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;

public class CrudAdapter extends RecyclerView.Adapter<CrudAdapter.ViewHolder> {

    private final List<HashMap<String, String>> crudList;
    private final OnEditButtonClickListener editButtonClickListener;
    private final OnDeleteButtonClickListener deleteButtonClickListener;

    public CrudAdapter(List<HashMap<String, String>> crudList, OnEditButtonClickListener editButtonClickListener, OnDeleteButtonClickListener deleteButtonClickListener) {
        this.crudList = crudList;
        this.editButtonClickListener = editButtonClickListener;
        this.deleteButtonClickListener = deleteButtonClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_crud, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HashMap<String, String> crudItem = crudList.get(position);

        String name = crudItem.get("name");
        String lastName = crudItem.get("lastName");
        String identificationNumber = crudItem.get("identificationNumber");
        String dob = crudItem.get("dob");
        String issue = crudItem.get("issue");
        String expiry = crudItem.get("expiry");

        holder.nameTv.setText(name);
        holder.lastNameTv.setText(lastName);
        holder.identificationNumberTv.setText(identificationNumber);
        holder.dobTv.setText(dob);
        holder.issueTv.setText(issue);
        holder.expiryTv.setText(expiry);

        // Set click listener for editBtn
        holder.editBtn.setOnClickListener(view -> {
            if (editButtonClickListener != null) {
                editButtonClickListener.onEditButtonClick(crudItem);
            }
        });

        // Set click listener for deleteBtn
        holder.deleteBtn.setOnClickListener(view -> {
            if (deleteButtonClickListener != null) {
                deleteButtonClickListener.onDeleteButtonClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return crudList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameTv;
        TextView lastNameTv;
        TextView identificationNumberTv;
        TextView dobTv;
        TextView issueTv;
        TextView expiryTv;
        ImageView editBtn;
        ImageView deleteBtn;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.recName);
            lastNameTv = itemView.findViewById(R.id.recLastName);
            identificationNumberTv = itemView.findViewById(R.id.recIdNumber);
            dobTv = itemView.findViewById(R.id.recDOB);
            issueTv = itemView.findViewById(R.id.recIssue);
            expiryTv = itemView.findViewById(R.id.recExpiry);
            editBtn = itemView.findViewById(R.id.editBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }

    // Interface for handling edit button clicks
    public interface OnEditButtonClickListener {
        void onEditButtonClick(HashMap<String, String> crudItem);
    }

    // Interface for handling delete button clicks
    public interface OnDeleteButtonClickListener {
        void onDeleteButtonClick(int position);
    }
}
