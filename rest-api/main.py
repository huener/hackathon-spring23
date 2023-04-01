from fastapi import FastAPI

app = FastAPI()

@app.post("/getGPTResponse/{search}")
async def getGPTResponse(search):
    # Send API request to ChatGPT
    # Store response in string
    # Store letter grade in string
    return  {"message": f"gpt response here + grade here in two strings"}
