import profile_pb2_grpc, TonalityModel
from profile_pb2 import TextVector
import spacy

nlp = spacy.load("ru_core_news_lg")

tags = {"NOUN"}


def get_vector(text):
    doc = nlp(text)
    return doc.vector


def get_facts(sent):
    visited = set()

    ans = dict()
    childs = get_childs(sent.root)
    for v in childs:
        ans[v] = get_vector(v)

    for word in sent:
        if word.tag_ in tags and word.head not in visited:
            childs = get_childs(word)
            for v in childs:
                ans[v] = get_vector(v)
    return ans


def get_childs_impl(node, cmp):
    text = node.lemma_
    for c in list(node.children):
        if cmp(c):
            text += " " + get_childs_impl(c, cmp)

    return text


relat = {"amod", "nsubj", "nsubj:pass", "obl", "nummod"}
relat_with_new = {"nmod", "flat:foreign"}


def get_childs(node):
    f = get_childs_impl(node,
                        lambda c: not c.is_stop and c.is_alpha and not c.is_oov and c.dep_ in relat)
    s = get_childs_impl(node,
                        lambda c: not c.is_stop and c.is_alpha and not c.is_oov and (
                                c.dep_ in relat or c.dep_ in relat_with_new))
    return f, s


class VectorizerImpl(profile_pb2_grpc.VectorizationService):

    @staticmethod
    def __vectorizeText(text):
        doc = nlp(text)
        sents = list(doc.sents)

        for sent in sents:
            facts = get_facts(sent)
            facts[sent.text] = sent.vector
            for fact_key in facts.keys():
                is_negative = TonalityModel.get_tonality(fact_key) == "negative"
                response = TextVector(is_negative=is_negative)
                response.vector.extend(facts[fact_key].tolist())
                yield response

    @staticmethod
    def VectorizeProposal(request, target, options=(), channel_credentials=None, call_credentials=None, insecure=False,
                          compression=None, wait_for_ready=None, timeout=None, metadata=None):
        text = ". ".join([request.description, request.title, request.location])
        yield from VectorizerImpl.__vectorizeText(text)

    @staticmethod
    def VectorizeRequest(request, target, options=(), channel_credentials=None, call_credentials=None, insecure=False,
                         compression=None, wait_for_ready=None, timeout=None, metadata=None):
        yield from VectorizerImpl.__vectorizeText(request.request)

