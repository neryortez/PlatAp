package io.github.rathn.platap.dto;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class to create Facturas and set them inside the Transactions. Under construction.
 */

public class Factura implements Parcelable {
    private String filePath;
    private String transactionId;
    private String id;
    private double price = 0d;

    /**
     * Creates a new Facturas with nothing inside
     */
    public Factura(){

    }

    /**
     *Create a new Factura with the data provided
     * @param filePath The path of the image of the Factura.
     * @param transactionId The ID of the transaction these Factura belongs to.
     * @param id The ID on database of these Factura.
     */
    public Factura(String filePath, String transactionId, String id) {
        this.filePath = filePath;
        this.transactionId = transactionId;
        this.id = id;
    }

    //<editor-fold desc="Parcalable Details">
    protected Factura(Parcel in) {
        filePath = in.readString();
        transactionId = in.readString();
        id = in.readString();
        price = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(filePath);
        dest.writeString(transactionId);
        dest.writeString(id);
        dest.writeDouble(price);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Factura> CREATOR = new Creator<Factura>() {
        @Override
        public Factura createFromParcel(Parcel in) {
            return new Factura(in);
        }

        @Override
        public Factura[] newArray(int size) {
            return new Factura[size];
        }
    };
    //</editor-fold>

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
