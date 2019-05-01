/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stockmarket;

import java.util.ArrayList;

/**
 *
 * @author hugo
 */
public class Asset {

    private String name;
    private double weigth;
    private ArrayList<Double> returns;
    private String portfolioType;

    public Asset(String name) {
        this.name = name;
        this.returns = new ArrayList<>();
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<Double> getReturns() {
        return this.returns;
    }

    public void addReturn(Double r) {
        this.returns.add(r);
    }

    public int size() {
        return this.returns.size();
    }

    public Double getReturn(int i) {
        return this.returns.get(i);
    }

    public void setReturns(ArrayList<Double> returns) {
        this.returns = returns;
    }

    public double getWeigth() {
        return weigth;
    }

    public void setWeigth(double weigth) {
        this.weigth = weigth;
    }

    public String getPortfolioType() {
        return portfolioType;
    }

    public void setPortfolioType(String portfolioType) {
        this.portfolioType = portfolioType;
    }

    public void setReturn(int s, double enhancedReturn) {
        this.returns.set(s, enhancedReturn);
    }
}
