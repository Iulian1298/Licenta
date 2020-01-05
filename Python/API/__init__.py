import uuid
import datetime
from functools import wraps
import os
import jwt
from flask import Flask, request, make_response, jsonify
from flask_api import status
from pip._vendor.appdirs import unicode
from werkzeug.utils import secure_filename
import base64
from datetime import date


app = Flask(__name__)
app.config['SECRET_KEY'] = "secret"
app.config['IMAGE_FOLDER'] = "..\\"
