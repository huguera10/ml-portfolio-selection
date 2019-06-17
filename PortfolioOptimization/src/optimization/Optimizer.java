/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package optimization;

import ilog.concert.IloException;
import io.InputOutput;
import java.io.FileNotFoundException;
import java.io.IOException;
import optimization.models.BaseOptimization;
import optimization.models.cvar.CvarOptimization;
import optimization.models.ssd.SSDOptimization;
import org.json.simple.parser.ParseException;
import parameters.Parameters;
import stockmarket.Asset;
import stockmarket.Portfolio;

/**
 *
 * @author hugo
 */
public class Optimizer {

    private Parameters params;
    private InputOutput io;

    public Optimizer(String parametersPath) throws IOException, FileNotFoundException, ParseException {
        params = new Parameters(parametersPath);
        io = new InputOutput();
    }

    public void optimize() throws IloException, IOException, FileNotFoundException, java.text.ParseException {
        Portfolio portfolio = io.readFile(params.getDataPath());
        portfolio.setAssetsNamesMap();

        int dateIndex = portfolio.getDateIndex(params.getDate());
        Portfolio simulationPortfolio = portfolio.getSimulationPortfolio(
                dateIndex, params.getHistoricalDays()
        );
        simulationPortfolio.preprocessPortfolioData();

        BaseOptimization optimizationModel = null;

        switch (params.getOptimizationType()) {
            case "cvar":
                optimizationModel = new CvarOptimization(
                        simulationPortfolio,
                        params
                );
                break;
            case "ssd":
                Asset trackedAsset = simulationPortfolio.getAsset(params.getTrackedAssetName());
                simulationPortfolio.dropAsset(params.getTrackedAssetName());

                optimizationModel = new SSDOptimization(
                        simulationPortfolio,
                        params,
                        trackedAsset
                );
                break;
            default:
                System.out.println("Param \"optimizationType\" is not valid.");
                System.exit(0);
        }

        if (optimizationModel.solve()) {
            io.writePortfoliosWeightsDictionary(params, simulationPortfolio);
        }

    }
}
