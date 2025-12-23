FROM python:3.13-slim

WORKDIR /inference

COPY requirements.txt /inference/requirements.txt

RUN pip install -r requirements.txt

COPY models/ /inference/models/

COPY src/ /inference/src/

CMD ["python", "-m", "src.server"]
