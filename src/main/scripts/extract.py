#!/usr/bin/env python3

import json

print("Capteur,Date,Valeur")
with open("data.js") as file:
    file.readline();
    for s in json.loads(file.readline()):
        for m in s["data"]:
            print("{},{},{}".format(s["name"],m[0],m[1]))
