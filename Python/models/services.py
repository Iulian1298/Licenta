from sqlalchemy.ext.hybrid import hybrid_method

from models import *

from database import db
from sqlalchemy import func


def calculateDistance(lat1, long1, latitude, longitude, math=math):
    if (lat1 == latitude) and (long1 == longitude):
        return 0
    else:
        lat1 = math.radians(lat1)
        long1 = math.radians(long1)
        lat2 = math.radians(latitude)
        long2 = math.radians(longitude)
        earthRadius = 6371.01
        return earthRadius * math.acos(
            math.sin(lat1) * math.sin(lat2) + math.cos(lat1) * math.cos(lat2) * math.cos(long1 - long2))


class Service(db.Model):
    _tablename_ = 'Services'
    id = db.Column(db.String, unique=True, primary_key=True)
    logoPath = db.Column(db.String)
    name = db.Column(db.String)
    description = db.Column(db.String)
    latitude = db.Column(db.Float)
    longitude = db.Column(db.Float)
    address = db.Column(db.String)
    city = db.Column(db.String)
    rating = db.Column(db.Float)
    phoneNumber = db.Column(db.String)
    email = db.Column(db.String)
    owner = db.Column(db.String)
    serviceType = db.Column(db.String)
    acceptedBrand = db.Column(db.String)
    priceService = db.Column(db.Integer)
    priceTire = db.Column(db.Integer)
    priceChassis = db.Column(db.Integer)
    priceItp = db.Column(db.Integer)
    distanceFromUser = db.Column(db.Float)

    def __repr__(self):
        return "id: {}, logoPath: {}, name: {}, description: {}, latitude: {},longitude: {}, address: {}, city: {}, " \
               "rating: {}, phoneNumber: {}, email: {}, owner: {}, serviceType: {}, acceptedBrand: {}, priceService: " \
               "{}, priceTire: {}, priceChassis: {}, priceItp: {}" \
            .format(self.id, self.logoPath, self.name, self.description, self.latitude, self.longitude, self.address,
                    self.city, self.rating, self.phoneNumber, self.email, self.owner, self.serviceType,
                    self.acceptedBrand, self.priceService, self.priceTire, self.priceChassis, self.priceItp)

    def toDict(self):
        return dict(zip(
            ["id", "logoPath", "name", "description", "latitude", "longitude", "address", "city",
             "rating", "phoneNumber", "email", "owner", "serviceType", "acceptedBrand", "priceService", "priceTire",
             "priceChassis", "priceItp"],
            self.toList()))

    def toList(self):
        return [self.id, self.logoPath, self.name, self.description, self.latitude, self.longitude, self.address,
                self.city, self.rating, self.phoneNumber, self.email, self.owner, self.serviceType, self.acceptedBrand,
                self.priceService, self.priceTire, self.priceChassis, self.priceItp]

    @hybrid_method
    def checkType(self, serviceType):
        return self.serviceType & serviceType

    @checkType.expression
    def checkType(cls, serviceType):
        return cls.serviceType & int(serviceType)

    @hybrid_method
    def distance(self, lat, lng):
        return calculateDistance(lat, lng, float(self.latitude), float(self.longitude))

    @distance.expression
    def distance(cls, lat, lng):
        return calculateDistance(lat, lng, cls.latitude.cast(db.Float), cls.longitude.cast(db.Float), math=func)
