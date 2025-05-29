-- SEQUENCE: public.dog_id_seq

-- DROP SEQUENCE IF EXISTS public.dog_id_seq;

CREATE SEQUENCE IF NOT EXISTS public.dog_id_seq
    INCREMENT 1
    START 100
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Table: public.dog

-- DROP TABLE IF EXISTS public.dog;

CREATE TABLE IF NOT EXISTS public.dog
(
    id integer NOT NULL DEFAULT nextval('dog_id_seq'::regclass),
    name character varying(100) COLLATE pg_catalog."default",
    owner character varying(100) COLLATE pg_catalog."default",
    description text COLLATE pg_catalog."default",
    CONSTRAINT dog_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.dog
    OWNER to postgres;

-- ALTER SEQUENCE: public.dog_id_seq

ALTER SEQUENCE public.dog_id_seq
    OWNED BY public.dog.id;

ALTER SEQUENCE public.dog_id_seq
    OWNER TO postgres;

-- INSERT: public.dog

INSERT INTO public.dog (name, owner, description) VALUES ('Prancer', 'Natalie', 'Small chihuahua with neurotic behavior, doesn't like men or children, prefers to be left alone but fiercely loyal.');
INSERT INTO public.dog (name, owner, description) VALUES ('Buddy', 'Jack', 'Playful golden retriever who loves fetch and swimming.');
INSERT INTO public.dog (name, owner, description) VALUES ('Luna', 'Emily', 'Calm husky with a gentle demeanor and expressive blue eyes.');
INSERT INTO public.dog (name, owner, description) VALUES ('Max', 'Ava', 'Energetic beagle who enjoys long walks and sniffing everything.');
INSERT INTO public.dog (name, owner, description) VALUES ('Bella', 'Ryan', 'Friendly labrador who gets along with kids and other dogs.');
INSERT INTO public.dog (name, owner, description) VALUES ('Charlie', 'Sophia', 'Curious corgi with a fondness for squeaky toys.');
INSERT INTO public.dog (name, owner, description) VALUES ('Rocky', 'Liam', 'Strong German shepherd trained in basic obedience.');
INSERT INTO public.dog (name, owner, description) VALUES ('Milo', 'Olivia', 'Cheerful shih tzu who enjoys naps and belly rubs.');
INSERT INTO public.dog (name, owner, description) VALUES ('Daisy', 'Noah', 'Smart border collie who excels in agility training.');
INSERT INTO public.dog (name, owner, description) VALUES ('Cooper', 'Isabella', 'Loyal rottweiler with a protective instinct but very affectionate.');