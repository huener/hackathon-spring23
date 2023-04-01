from fastapi import FastAPI
import psycopg2

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

@app.get("/")
async def root():
    return {"Welcome to my silly little API"}

@app.get("/WholeCart")
async def WholeCart():
    openDB()
    result = executeSelect('''
                            SELECT * from "Cart"
                            ''')
    closeDB()


