package transaction.entity;

import transaction.InvalidIndexException;

/**
 * @author Duocai Wu
 * @Date 2019/7/19
 * @Time 15:16
 */
public class Customer extends ResourceItem {
    private String custName;

    public Customer(String custName) {
        this.custName = custName;
    }

    @Override
    public Object getIndex(String indexName) throws InvalidIndexException {
        return new InvalidIndexException(indexName);
    }

    @Override
    public Object getKey() {
        return custName;
    }

    @Override
    public Object clone() {
        return new Customer(custName);
    }
}
