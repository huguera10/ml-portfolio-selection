#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Monday Jun 25 23:42:26 2019

@author: hugo

usage: python generate_machine_learning_dataset.py \
    --prices ../data/bovespa_diario_close_parsed.csv \
    --jsons ../portfolio_weights/cvar/alpha_2_5 --broker cvar_alpha_2_5
"""

import glob
import json
import pandas as pd
from argparse import ArgumentParser
from portfolio_instance import PortfolioInstance


class GenerateMachineLearningDataset(object):

    def __init__(self, prices_data_path, jsons_data_folder, broker_base_name):
        self.prices_data_path = prices_data_path
        self.jsons_data_folder = jsons_data_folder
        self.broker_base_name = broker_base_name

    def read_prices_data(self):
        df_prices = pd.read_csv(self.prices_data_path, parse_dates=["date"])
        return df_prices

    def parse_prices_to_returns(self, df_prices):
        stock_codes = [col for col in df_prices.columns if col != "date"]

        df_prices[stock_codes] = df_prices[stock_codes].pct_change()

    def group_data_by_month(self, df):
        def cumprod(values):
            result = 1.0
            for value in values:
                result = result * (1 + value)

            return result - 1.0

        df.index = df["date"]
        stock_codes = [col for col in df.columns if col != "date"]

        df_month = pd.DataFrame(
            index=pd.date_range(
                start=df["date"].min(),
                end=df["date"].max(),
                freq="MS"
            )
        )
        df_grouped = df.groupby(pd.Grouper(freq="MS"))

        for stock_code in stock_codes:
            df_month[stock_code] = df_grouped[stock_code].apply(
                lambda x: cumprod(x.values))

        return df_month

    def _get_json_file_paths(self):
        json_file_paths = glob.glob(
            "{}/*portfolio_weights.json".format(self.jsons_data_folder))

        return json_file_paths

    def _get_stock_return(self, df_returns_grouped, date, stock_code):
        stock_return = df_returns_grouped.loc[date][stock_code]
        return stock_return

    def iterate_over_json_files_and_create_portfolios(self, df_returns_grouped):
        portfolios_list = []

        for json_file_path in self._get_json_file_paths():
            with open(json_file_path, "r") as json_file:
                json_data = json.load(json_file)

            json_file_name = json_file_path.split("/")[-1]
            portfolio_date = json_file_name.split("_")[0]

            portfolio = PortfolioInstance(
                name="{}_{}".format(
                    self.broker_base_name,
                    "_".join(json_file_name.split("_")[1:]).replace(".json", "")),
                date=portfolio_date
            )

            portfolio_return = 0.0
            for stock_code, weight in json_data["PORTFOLIO"].items():
                portfolio.add_stock_code_and_weight(stock_code, weight)

                stock_return = self._get_stock_return(
                    df_returns_grouped,
                    pd.to_datetime(portfolio_date),
                    stock_code
                )

                portfolio_return += (stock_return * float(weight))

            portfolio.set_portfolio_return(portfolio_return)

            portfolios_list.append(portfolio.to_dataframe())

        return portfolios_list

    def concat_portfolios_list(self, portfolios_list):
        df_portfolios = pd.concat(
            portfolios_list,
            ignore_index=True,
            sort=False
        )
        return df_portfolios

    def store_df_portfolios_to_file(self, df_portfolios):
        df_portfolios.to_csv(
            "{}/df_{}.csv".format(self.jsons_data_folder,
                                  self.broker_base_name),
            index=False
        )

    def run(self):
        df_prices = self.read_prices_data()

        self.parse_prices_to_returns(df_prices)

        df_returns_grouped = self.group_data_by_month(df_prices)

        portfolios_list = self.iterate_over_json_files_and_create_portfolios(
            df_returns_grouped)

        df_portfolios = self.concat_portfolios_list(portfolios_list)

        self.store_df_portfolios_to_file(df_portfolios)


if __name__ == "__main__":
    parser = ArgumentParser()

    parser.add_argument("--prices-data-path", type=str, required=True)
    parser.add_argument("--jsons-data-folder", type=str, required=True)
    parser.add_argument("--broker-base-name", type=str, required=True)

    args = parser.parse_args()

    generate_machine_learning_dataset = GenerateMachineLearningDataset(
        prices_data_path=args.prices_data_path,
        jsons_data_folder=args.jsons_data_folder,
        broker_base_name=args.broker_base_name
    )
    generate_machine_learning_dataset.run()
