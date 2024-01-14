import profile_pb2_grpc, TonalityModel
from profile_pb2 import TextVector, VectorizedProposal
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
    def analyse(doc):
        sents = list(doc.sents)

        for sent in sents:
            facts = get_facts(sent)
            facts[sent.text] = sent.vector
            is_negative_list = TonalityModel.get_tonality_vec(list(facts.keys()))
            i = 0
            for fact_key in facts.keys():
                # is_negative = TonalityModel.get_tonality(fact_key) == "negative"
                is_negative = is_negative_list[i] == "negative"
                i += 1
                # is_negative = False
                response = TextVector(vector=facts[fact_key].tolist(), is_negative=is_negative)
                yield response

    @staticmethod
    def VectorizeProposals(request_iterator, target, options=(), channel_credentials=None, call_credentials=None,
                           insecure=False, compression=None, wait_for_ready=None, timeout=None, metadata=None):

        try:
            text = map(lambda v: ". ".join([v.description, v.title, v.location]), request_iterator)
            for doc in nlp.pipe(text, batch_size=1):
                ans = VectorizerImpl.analyse(doc)
                yield VectorizedProposal(vector=ans)
        except Exception as e:
            print(e)



    @staticmethod
    def VectorizeRequest(request, target, options=(), channel_credentials=None, call_credentials=None, insecure=False,
                         compression=None, wait_for_ready=None, timeout=None, metadata=None):
        yield from VectorizerImpl.analyse(nlp(request.request))