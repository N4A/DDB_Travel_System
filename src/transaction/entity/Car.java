package transaction.entity;

import transaction.InvalidIndexException;

/**
 * @author Duocai Wu
 * @Date 2019/7/19
 * @Time 15:09
 */
public class Car extends ResourceItem {
    private String location;
    private int price; // every car has the same price
    private int numCars;
    private int numAvail;

    public Car(String location, int price, int numCars) {
        this.location = location;
        this.price = price;
        this.numCars = numCars;
        this.numAvail = numCars;
    }

    private Car(String location, int price, int numCars, int numAvail) {
        this.location = location;
        this.price = price;
        this.numCars = numCars;
        this.numAvail = numAvail;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getNumCars() {
        return numCars;
    }

    public int getNumAvail() {
        return numAvail;
    }

    public void bookCars(int num) {
        this.numAvail -= num;
    }

    public void unbookCars(int num) {
        this.numAvail += num;
    }

    public void addCars(int num) {
        this.numCars += num;
        this.numAvail += num;
    }

    public void deleteCars(int num) {
        this.numCars -= num;
        this.numAvail -= num;
    }

    @Override
    public Object getIndex(String indexName) throws InvalidIndexException {
        return null;
    }

    @Override
    public Object getKey() {
        return location;
    }

    @Override
    public Object clone() {
        return new Car(location, price, numCars, numAvail);
    }
}
