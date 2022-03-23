# -*- coding: utf-8 -*-
"""
Created on Thu Mar 25 15:54:26 2021

@author: Stefan
"""
import pandas as pd
import numpy as np
import pickle
# Using Skicit-learn to split data into training and testing sets
from sklearn.model_selection import train_test_split

from sklearn.tree import export_graphviz
import pydot

# Import the model we are using
from sklearn.ensemble import RandomForestClassifier
from sklearn.feature_extraction.text import CountVectorizer
# Import tools needed for visualization
from sklearn.tree import export_graphviz
import pydot
import sklearn.metrics as metrics
from IPython.display import Image

import os


features = pd.read_csv("teste10D.csv")
features.head()
#features.describe()

# Labels are the values we want to predict
labels = np.array(features['AVGND'])

# Remove the labels from the features
# axis 1 refers to the columns
features = features.drop('AVGND', axis = 1)


# Saving feature names for later use
feature_list = list(features.columns)
# Convert to numpy array
features = np.array(features)
# Split the data into training and testing sets
#labels = pd.get_dummies(labels)

train_features, test_features, train_labels, test_labels = train_test_split(features, labels, test_size = 0.25, random_state = 42)

print('Training Features Shape:', train_features.shape)
print('Training Labels Shape:', train_labels.shape)
print('Testing Features Shape:', test_features.shape)
print('Testing Labels Shape:', test_labels.shape)


X = pd.get_dummies(train_labels)
Y = pd.get_dummies(test_labels)
print(X)
# Instantiate model with 1000 decision trees
rf = RandomForestClassifier(n_estimators = 10, random_state = 42, max_depth = 3)

# Train the model on training data
rf.fit(train_features, X);
# Use the forest's predict method on the test data
predictions = rf.predict(test_features)
print("Accuracy:",metrics.accuracy_score(Y, predictions))
# Calculate the absolute errors
#errors = abs(predictions - test_labels)
filename = 'teste.sav'
pickle.dump(rf, open(filename, 'wb'))

loaded_model = pickle.load(open(filename, 'rb'))
result = loaded_model.score(test_features, Y)
print(result)
