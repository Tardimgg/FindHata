from dostoevsky.tokenization import RegexTokenizer
from dostoevsky.models import FastTextSocialNetworkModel


tokenizer = RegexTokenizer()
model = FastTextSocialNetworkModel(tokenizer=tokenizer)

def get_tonality(text):

    res = model.predict([text], k=2)
    if "negative" in res[0] and res[0]["negative"] > 0.2:
        return "negative"
    else:
        return "neutral"
