/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package stockmarket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;

/**
 *
 * @author hugo
 */
public class Portfolio {

    private ArrayList<String> dates;
    private ArrayList<Asset> assets;
    private HashMap<String, Integer> assetsNamesMap;
    private double[][] covarianceMatrix;

    public Portfolio() {
        this.dates = new ArrayList<>();
        this.assets = new ArrayList<>();
        this.assetsNamesMap = new HashMap<>();
        covarianceMatrix = null;
    }

    public void parsePricesToReturns() {
        for (int i = 0; i < assets.size(); i++) {
            Asset asset = assets.get(i);

            ArrayList<Double> returns = new ArrayList<>(asset.size());
            returns.add(0.0);
            for (int j = 1; j < asset.size(); j++) {
                if (asset.getReturn(j - 1) > 0.0) {
                    returns.add((asset.getReturn(j) - asset.getReturn(j - 1)) / asset.getReturn(j - 1));
                } else {
                    returns.add(0.0);
                }
            }

            assets.get(i).setReturns(returns);
        }
    }

    public void parsePricesToLogReturns() {
        for (int i = 0; i < assets.size(); i++) {
            Asset asset = assets.get(i);

            ArrayList<Double> returns = new ArrayList<>(asset.size());
            returns.add(0.0);
            for (int j = 1; j < asset.size(); j++) {
                if (asset.getReturn(j) > 0.0 && asset.getReturn(j - 1) > 0.0) {
                    returns.add(Math.log(asset.getReturn(j)) - Math.log(asset.getReturn(j - 1)));
                } else {
                    returns.add(0.0);
                }
            }

            assets.get(i).setReturns(returns);
        }
    }

    public void dropAssetsWithMissingData() {
        for (int i = assets.size() - 1; i >= 0; i--) {
            Asset asset = assets.get(i);

            for (int j = 0; j < asset.size(); j++) {
                if (asset.getReturn(j) <= 0.0) {
                    assets.remove(i);
                    break;
                }
            }
        }

    }

    public Portfolio getPortfolioSlice(String initialDate, String finalDate, double zerosThreshold) {
        Portfolio portfolio = new Portfolio();
        int initialIdx = -1;
        int finalIdx = -1;

        for (int i = 0; i < this.dates.size(); i++) {
            if (this.dates.get(i).compareTo(initialDate) >= 0) {
                initialIdx = i;
            }
            if (this.dates.get(i).compareTo(finalDate) < 0) {
                finalIdx = i;
            }
        }

        for (Asset asset : assets) {
            portfolio.addAsset(new Asset(asset.getName()));
        }

        for (int i = initialIdx; i <= finalIdx; i++) {
            portfolio.addDate(this.dates.get(i));

            for (int j = 0; j < assets.size(); j++) {
                Asset asset = assets.get(j);
                portfolio.addReturnToAsset(asset.getReturn(i), j);
            }
        }

        portfolio.dropAssetsWithMissingData();
        return portfolio;
    }

    public int getDateIndex(String date) {
        for (int i = 0; i < this.dates.size(); i++) {
            if (dates.get(i).compareTo(date) >= 0) {
                return i;
            }
        }

        return -1;
    }

    public Portfolio getSimulationPortfolio(int index, int historicalDays) {
        Portfolio portfolio = new Portfolio();
        int finalIdx = index;
        int initialIdx = finalIdx - historicalDays;

        if (initialIdx < 0) {
            initialIdx = 0;
        }

        for (Asset asset : assets) {
            portfolio.addAsset(new Asset(asset.getName()));
        }

        for (int i = initialIdx; i <= finalIdx; i++) {
            portfolio.addDate(this.dates.get(i));

            for (int j = 0; j < assets.size(); j++) {
                Asset asset = assets.get(j);
                portfolio.addReturnToAsset(asset.getReturn(i), j);
            }
        }

        return portfolio;
    }

    public void preprocessPortfolioData() {
//        this.parsePricesToReturns();
        this.dropAssetsWithMissingData();
        this.parsePricesToLogReturns();
        this.setAssetsNamesMap();
    }

    public ArrayList<Double> getAssetsPrices(int dateIndex, LinkedList<String> assetsNames) {
        ArrayList<Double> prices = new ArrayList();

        for (String assetName : assetsNames) {
            Asset asset = assets.get(this.assetsNamesMap.get(assetName));

            prices.add(asset.getReturn(dateIndex));
        }

        return prices;
    }

    public double[] getAssetsMeanReturn() {
        double[] assetsMeanReturns = new double[assets.size()];
        for (int i = 0; i < assets.size(); i++) {
            assetsMeanReturns[i] = getAssetMeanReturn(i);
        }
        return assetsMeanReturns;
    }

    public double getAssetMeanReturn(int assetId) {
        Asset asset = this.assets.get(assetId);

        double sum = 0;
        for (int i = 0; i < asset.size(); i++) {
            sum += asset.getReturn(i);
        }

        return sum / asset.size();
    }

    public double getPortfolioMeanReturn() {
        double assetsMeanReturnSum = 0.0;
        for (int i = 0; i < this.assets.size(); i++) {
            assetsMeanReturnSum += getAssetMeanReturn(i);
        }
        return assetsMeanReturnSum / this.assets.size();
    }

    public void dropAsset(String name) {
        for (int i = 0; i < assets.size(); i++) {
            Asset asset = assets.get(i);
            if (asset.getName().equals(name)) {
                assets.remove(i);
                return;
            }
        }
    }

    private void computeCovarianceMatrix() {
        covarianceMatrix = new double[getN()][getN()];
        double[][] returnsMatrix = new double[getS()][getN()];

        for (int i = 0; i < getN(); i++) {
            for (int j = 0; j < getS(); j++) {
                returnsMatrix[j][i] = assets.get(i).getReturn(j);
            }
        }

        RealMatrix covMatrix = new Covariance(returnsMatrix).getCovarianceMatrix();

        for (int i = 0; i < getN(); i++) {
            for (int j = 0; j < getN(); j++) {
                covarianceMatrix[i][j] = covMatrix.getEntry(i, j);
            }
        }
    }

    public double[][] getCovarianceMatrix() {
        if (covarianceMatrix == null) {
            computeCovarianceMatrix();
        }

        return covarianceMatrix;
    }

    public void dropFirstAssetsReturn() {
        for (int i = 0; i < getN(); i++) {
            assets.get(i).getReturns().remove(0);
        }
        dates.remove(0);
    }

    public void addDate(String date) {
        this.dates.add(date);
    }

    public void addAsset(Asset asset) {
        this.assets.add(asset);
    }

    public ArrayList<Asset> getAssets() {
        return this.assets;
    }

    public Asset getAsset(int i) {
        return this.assets.get(i);
    }

    public Asset getAsset(String assetName) {
        return this.assets.get(this.assetsNamesMap.get(assetName));
    }

    public int getTotalAssets() {
        return this.assets.size();
    }

    public int getN() {
        return this.getTotalAssets();
    }

    public int getTotalReturns() {
        return this.assets.get(0).size();
    }

    public int getS() {
        return this.getTotalReturns();
    }

    public void addReturnToAsset(Double r, int i) {
        this.assets.get(i).addReturn(r);
    }

    public void setAssetsNamesMap() {
        for (int i = 0; i < this.getN(); i++) {
            this.assetsNamesMap.put(this.assets.get(i).getName(), i);
        }
    }

    public void setAssetWeight(int i, double d, String portfolioType) {
        this.assets.get(i).setWeigth(d);
        this.assets.get(i).setPortfolioType(portfolioType);
    }

    public void setAssetWeight(int i, double d) {
        this.assets.get(i).setWeigth(d);
    }

    public double getAssetWeight(int i) {
        return this.assets.get(i).getWeigth();
    }

    public String getAssetName(int i) {
        return this.assets.get(i).getName();
    }

    public int getAssetIndex(String assetName) {
        return this.assetsNamesMap.get(assetName);
    }

    public String getFirstDate() {
        return dates.get(0);
    }

    public String getLastDate() {
        return dates.get(dates.size() - 1);
    }

    public String getDateGivenIndex(int dateIndex) {
        return this.dates.get(dateIndex);
    }
}
