#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tuesday Jun 26 00:14:26 2019

@author: hugo
"""

import pandas as pd


class PortfolioInstance(object):

    def __init__(self, name, date):
        self.name = name
        self.date = date

        self.weights_list = []
        self.stock_codes_list = []
        self.portfolio_return = None

    def add_stock_code(self, stock_code):
        self.stock_codes_list.append(stock_code)

    def add_weight(self, weight):
        self.weights_list.append(weight)

    def add_stock_code_and_weight(self, stock_code, weight):
        self.stock_codes_list.append(stock_code)
        self.weights_list.append(weight)

    def set_portfolio_return(self, portfolio_return):
        self.portfolio_return = portfolio_return

    def to_dataframe(self):
        df = pd.DataFrame(columns=[
            "date",
            "name"
        ] + [
            "stock_code_{}".format(i)
            for i in range(1, len(self.stock_codes_list)+1)
        ] + [
            "weight_{}".format(i)
            for i in range(1, len(self.weights_list)+1)
        ] + [
            "portfolio_return"
        ])

        df.loc[0] = [
            self.date,
            self.name
        ] + self.stock_codes_list + \
            self.weights_list + [
            self.portfolio_return
        ]

        return df
