package transaction.entity;

import transaction.InvalidIndexException;

/**
 * @author Duocai Wu
 * @Date 2019/7/19
 * @Time 14:46
 */
public class Flight extends ResourceItem {
    private String flightNum;
    private int price; // every seat has the same price
    private int numSeats;
    private int numAvail;

    public Flight(String flightNum, int price, int numSeats) {
        this.flightNum = flightNum;
        this.price = price;
        this.numSeats = numSeats;
        this.numAvail = numSeats;
    }

    private Flight(String flightNum, int price, int numSeats, int numAvail) {
        this.flightNum = flightNum;
        this.price = price;
        this.numSeats = numSeats;
        this.numAvail = numAvail;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getNumSeats() {
        return numSeats;
    }

    public int getNumAvail() {
        return numAvail;
    }

    public void bookSeats(int num) {
        this.numAvail -= num;
    }

    public void unbookSeats(int num) {
        this.numAvail += num;
    }

    public void addSeats(int numSeats) {
        this.numSeats += numSeats;
        this.numAvail += numSeats;
    }

    @Override
    public Object getIndex(String indexName) throws InvalidIndexException {
        throw new InvalidIndexException(indexName);
    }

    @Override
    public Object getKey() {
        return flightNum;
    }

    @Override
    public Object clone() {
        return new Flight(this.flightNum, this.price, this.numSeats, this.numAvail);
    }
}
