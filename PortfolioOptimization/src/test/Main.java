/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import ilog.concert.IloException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import optimization.Optimizer;

/**
 *
 * @author hugo
 */
public class Main {

    public static void main(String[] args) throws IOException, IloException, FileNotFoundException, ParseException, org.json.simple.parser.ParseException {

        String parametersPath;
        if (args.length > 0) {
            parametersPath = args[0];
        } else {
            parametersPath = "../portfolio_weights/opt_base_config.json";
        }

        Optimizer optimizer = new Optimizer(parametersPath);
        optimizer.optimize();
                
        System.exit(0);
    }
}
