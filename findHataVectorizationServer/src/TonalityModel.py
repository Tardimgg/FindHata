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


def get_tonality_vec(vec):
    tonality = ["neutral" for _ in range(len(vec))]
    res = model.predict(vec, k=2)
    for i in range(len(res)):
        if "negative" in res[0] and res[0]["negative"] > 0.2:
            tonality[i] = "negative"
        else:
            tonality[i] = "neutral"
    return tonality

