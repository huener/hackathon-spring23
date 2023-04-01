#!/usr/bin/env python3

from fastapi import FastAPI
import json
import requests
import openai
import psycopg2
import uvicorn

def openDB():
    global conn
    conn = psycopg2.connect(
        user="postgres",
        password="Ie0prHyD9JZS0hM7",
        host="db.lroakeegvjazrfrsdccb.supabase.co",
        port="5432"
    )

    global cursor
    cursor = conn.cursor()

def closeDB():
    cursor.close()
    conn.close()

def executeSelect(query):
    cursor.execute(query)
    return cursor.fetchall()

def executeSelect2(query, data):
    cursor.execute(query, data)
    return cursor.fetchall()

def insert(query, data):
    cursor.execute(query, data)
    conn.commit()

def executeUpdate(query, data):  # use this function for delete and update
    cursor.execute(query, data)
    conn.commit()

app = FastAPI()

@app.get("/WholeCart")
async def WholeCart():
    result = executeSelect('''
                            SELECT * from "Cart"
                            ''')
    return  {"message": f"{result}"}

# Data Sources
@app.get("/getChatGPTResponse/{product_company}")
async def getChatGPTResponse(product_company):
    API_KEY="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"
    prompt = f"On a scale of 1 to 13, what would you rate the sustainability of the company, {product_company}? Answer with only a single number, with nothing else in your response, including punctuation. Your response will only contain a single character. If you cannot access the most up-to-date information, try your best guess."
    openai.api_key = API_KEY
    response = openai.ChatCompletion.create(
            model="gpt-3.5-turbo",
            messages=[
                {"role": "user", "content": prompt}
            ]
    )
    grade = response['choices'][0]['message']['content']
    return  {
        "message": grade
    }

@app.get("/querySustainabilityDB/{upc}")
async def querySustainabilityDB(upc):
    return  {"message": f"todo"}

# TODO
@app.get("/crowdSourcedThing/{upc}")
async def crowdSourcedThing(upc):
    return  {"message": f"todo"}

# Get item info
@app.get("/getItemInfo/{upc}")
async def getItemInfo(upc):
    # Get request to that one upc database
    response = requests.get(f"https://api.upcitemdb.com/prod/trial/lookup?upc={upc}")

    # Reduce response to what we need
    data = {}
    data['brand'] = response['items'][0]['brand']
    data['name'] = response['items'][0]['title']
    data['image'] = response['items'][0]['images'][0]

    return json.dumps(data)

# Add item to cart

# Get cart item list

if __name__ == '__main__':
    openDB()
    uvicorn.run(app, port=8000, host='0.0.0.0')

