import "dotenv/config";

function required(name: string): string {
    const value = process.env[name];
    if (!value?.trim()) {
        throw new Error(`Falta configurar ${name}`);
    }
    return value;
}

function parsePort(value: string, name: string): number {
    const port = Number(value);
    if (!/^\d+$/.test(value) || port < 1 || port > 65535) {
        throw new Error(`${name} debe ser un puerto entre 1 y 65535`);
    }
    return port;
}

export const config = {
    port: parsePort(process.env.PORT ?? "3000", "PORT"),
    jwtSecret: required("JWT_SECRET"),
    database: {
        host: required("DB_HOST"),
        port: parsePort(required("DB_PORT"), "DB_PORT"),
        user: required("DB_USER"),
        password: required("DB_PASSWORD"),
        database: required("DB_NAME"),
    },
};
