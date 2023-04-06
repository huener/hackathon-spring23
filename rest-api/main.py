#!/usr/bin/env python3

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import json
import requests
import openai
import psycopg2
from starlette.requests import empty_receive
import uvicorn
import os

DB_FILE_NAME = 'data.json'

def initJsonDB():
    # Init json file
    json_file = open(DB_FILE_NAME, 'w')
    json_file.write(json.dumps({
        "reviews" : [],
        "cart" : []
    }))
    json_file.close()

def openDB():
    try:
        with open(DB_FILE_NAME,"r") as f:
            # Check that file is valid json
            # and contains 'reviews' and 'cart'
            json_thing = json.load(f)
            if 'reviews' not in json_thing:
                initJsonDB()
            elif 'cart' not in json_thing:
                initJsonDB()
    except IOError:
        initJsonDB()

    # Load json file
    json_file = open('data.json', 'r')
    global json_db
    json_db = json.load(json_file)
    json_file.close()

def writeJson():
    with open('data.json', 'w') as f:
        json.dump(json_db, f)

def addDBReview(upc, gptgrade, upvotes, downvotes):
    json_db['reviews'].append({
        "upc": upc,
        "gptgrade": int(gptgrade),
        "upvotes": upvotes,
        "downvotes": downvotes
    })
    writeJson()

def addDBCartItem(upc, name, imageLink, average_rating):
    json_db['cart'].append({
        "upc": upc,
        "name": name,
        "image": imageLink,
        "average_rating": int(average_rating)
    })
    writeJson()

def removeDBReview(upc):
    for review in json_db['reviews'][:]:
        if review['upc'] == upc:
            json_db['reviews'].remove(review)
            break
    writeJson()

def removeDBCartItem(upc):
    for cart_item in json_db['cart'][:]:
        if cart_item['upc'] == upc:
            json_db['cart'].remove(cart_item)
            break
    writeJson()

def readDBReview(upc):
    for review in json_db['reviews']:
        if review['upc'] == upc:
            return review
    return {}

def readDBCartItem(upc):
    for cart_item in json_db['cart']:
        if cart_item['upc'] == upc:
            return cart_item
    return {}


origins = [
    "http://api.arianb.me:8000",
    "*"
]

app = FastAPI()
openDB()

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

def closeDB():
    json_db.close()

@app.get("/")
async def root():
    return {"Welcome to my silly little API"}

@app.get("/WholeCart")
async def WholeCart():
    return  json_db['cart']

# Data Sources
@app.get("/getChatGPTResponse/")
async def getChatGPTResponse(upc, company):
    result = readDBReview(upc)
    if result != {}:
        gpt_rating = result['gptgrade']
        return  {
            "message": gpt_rating
        }

    prompt = f"On a scale of 1 to 13, what would you rate the sustainability of the company, {company}? Answer with only a single number, with nothing else in your response, including punctuation. Your response will only contain a single character. If you cannot access the most up-to-date information, try your best guess."
    file = open("./key.txt", 'r')
    openai.api_key = file.read()
    response = openai.ChatCompletion.create(
            model="gpt-3.5-turbo",
            messages=[
                {"role": "user", "content": prompt}
            ]
    )
    grade = response['choices'][0]['message']['content']
    addDBReview(upc, grade, 0, 0)
    return  {
        "message": grade
    }


@app.get("/getCrowdSourcedItemData/{upc}")
async def getCrowdSourcedItemData(upc):
    result = readDBReview(upc)
    if result == {}:
        return  {"rating": f"1"}
    upvotes = result['upvotes']
    downvotes = result['downvotes']
    score = 0
    if upvotes + downvotes == 0:
        score = 0
    else:
        score = round(13 * (upvotes / (upvotes + downvotes)))
    return  {"rating": f"{score}"}

# Get item info
@app.get("/getItemInfo/{upc}")
async def getItemInfo(upc):
    result = readDBCartItem(upc)
    if result != {}:
        return json.dumps(result)

    # Get request to that one upc database
    response = requests.get(f"https://api.upcitemdb.com/prod/trial/lookup?upc={upc}")
    response_json = response.json()

    # Reduce response to what we need
    data = {}

    if not 'items' in response_json:
        return {}

    if len(response_json['items']) == 0:
        return {}

    # Get average rating (query sources)
    crowdSourcedRating = await(getCrowdSourcedItemData(upc))
    brand = response_json['items'][0]['brand']
    gpt_rating = await(getChatGPTResponse(upc, brand))

    average_rating = (int(crowdSourcedRating["rating"]) + int(gpt_rating['message']))/2

    data['average_rating'] = average_rating
    brand_name = brand
    product_name = response_json['items'][0]['title']
    data['name'] = f"{brand_name} {product_name}"

    if len(response_json['items'][0]['images']) == 0:
        data['image'] = ""
    else:
        data['image'] = response_json['items'][0]['images'][0]
    data['upc'] = upc
    addDBCartItem(upc, data['name'], data['image'], average_rating)

    return json.dumps(data)

@app.get("/removeItem/{upc}")
async def removeItem(upc):
    removeDBCartItem(upc)
    return { "status": "0" }

@app.get("/removeItemReview/{upc}")
async def removeItemReview(upc):
    removeDBReview(upc)
    return { "status": "0" }

# Add item to cart

if __name__ == '__main__':
    openDB()
    uvicorn.run(app, port=8000, host='0.0.0.0')

