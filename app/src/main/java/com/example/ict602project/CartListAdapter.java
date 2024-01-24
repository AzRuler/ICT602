package com.example.ict602project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CartListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<CartItem> cartItems;


    public CartListAdapter(Context context, ArrayList<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;

    }


    @Override
    public int getCount() {
        return cartItems.size();
    }

    @Override
    public Object getItem(int position) {
        return cartItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }




    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.cart_list_item, parent, false);
            holder = new ViewHolder();
            holder.itemNameTextView = convertView.findViewById(R.id.itemNameTextView);
            holder.itemQuantityTextView = convertView.findViewById(R.id.itemQuantityTextView);
            holder.itemPriceTextView = convertView.findViewById(R.id.itemPriceTextView);
            holder.deleteButton = convertView.findViewById(R.id.deleteButton);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (cartItems != null && position < cartItems.size()) {
            CartItem cartItem = cartItems.get(position);

            if (cartItem != null) {
                holder.itemNameTextView.setText(cartItem.getItemName());
                holder.itemQuantityTextView.setText(String.format(Locale.US, "%d *", cartItem.getQuantity()));
                holder.itemPriceTextView.setText(String.format(Locale.US, "RM %.2f ", cartItem.getItemPrice()));
            }


        }

        return convertView;
    }



    // Method to calculate the total amount for the cart
    public double calculateTotalAmount() {
        double totalAmount = 0;

        for (int i = 0; i < getCount(); i++) {
            CartItem cartItem = (CartItem) getItem(i);
            if (cartItem != null) {
                totalAmount += cartItem.getTotalPrice();
            }
        }

        return totalAmount;
    }

    // ViewHolder pattern
    static class ViewHolder {
        TextView itemNameTextView;
        TextView itemPriceTextView;

        TextView itemQuantityTextView;

        Button deleteButton;
    }

    // Method to add a single item to the cart
    public void add(CartItem cartItem) {
        cartItems.add(cartItem);
        notifyDataSetChanged();
    }

    // Method to clear the cart items
    public void clear() {
        cartItems.clear();
        notifyDataSetChanged();
    }
    public void removeItem(int position) {
        if (position >= 0 && position < cartItems.size()) {
            cartItems.remove(position);
            notifyDataSetChanged();
        }
    }
}
